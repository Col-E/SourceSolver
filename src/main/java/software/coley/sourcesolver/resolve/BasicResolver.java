package software.coley.sourcesolver.resolve;

import software.coley.sourcesolver.model.*;
import software.coley.sourcesolver.resolve.entry.*;
import software.coley.sourcesolver.resolve.result.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.resolve.entry.PrimitiveEntry.*;
import static software.coley.sourcesolver.resolve.result.Resolutions.*;

public class BasicResolver implements Resolver {
	private final Map<String, ClassEntry> importedTypes;
	private final CompilationUnitModel unit;
	private final EntryPool pool;

	public BasicResolver(@Nonnull CompilationUnitModel unit, @Nonnull EntryPool pool) {
		this.unit = unit;
		this.pool = pool;

		importedTypes = Collections.unmodifiableMap(populateImports());
	}

	@Nonnull
	private Map<String, ClassEntry> populateImports() {
		Map<String, ClassEntry> map = new TreeMap<>();
		if (unit.getPackage().resolve(this) instanceof PackageResolution resolvedPackage) {
			pool.getClassesInPackage(resolvedPackage.getPackageName())
					.forEach(entry -> map.put(entry.getName(), entry));
		}
		for (ImportModel imp : unit.getImports()) {
			Resolution resolution = imp.resolve(this);
			if (resolution instanceof ClassResolution resolvedImport) {
				ClassEntry entry = resolvedImport.getClassEntry();
				map.put(entry.getName(), entry);
			} else if (resolution instanceof MultiClassResolution resolvedImport) {
				resolvedImport.getClassEntries()
						.forEach(entry -> map.put(entry.getName(), entry));
			}
		}
		for (ClassEntry entry : pool.getClassesInPackage("java/lang"))
			map.put(entry.getName(), entry);
		return map;
	}

	@Nonnull
	@Override
	public Resolution resolveAt(int index, @Nullable Model target) {
		if (target != null)
			return resolve(target);

		// Find the deepest model at position.
		Model model = unit;
		while (true) {
			Model child = model.getChildAtPosition(index);
			if (child == null)
				break;
			model = child;
		}

		// Resolve off of the deepest model so that it is aware of the results and can cache them.
		return model.resolve(this);
	}

	@Nonnull
	private Resolution resolve(@Nonnull Model target) {
		if (target instanceof ClassModel clazz) {
			return resolveClassModel(clazz);
		} else if (target instanceof MethodModel method) {
			return resolveMethodModel(method);
		} else if (target instanceof VariableModel variable) {
			if (target.getParent() instanceof ClassModel declaringClass)
				return resolveFieldModel(declaringClass, variable);
			return resolveVariableType(variable);
		} else if (target instanceof PackageModel pkg) {
			return resolvePackageModel(pkg);
		} else if (target instanceof ImportModel imp) {
			return resolveImportModel(imp);
		} else if (target instanceof ModifiersModel modifiers
				&& modifiers.getParent() instanceof MethodModel method
				&& method.getName().equals("<clinit>")) {
			return resolveStaticInitializer(method);
		} else if (target instanceof AnnotationArgumentModel annotation)
			return unknown(); // TODO: Annotations
		else if (target instanceof AnnotationExpressionModel annotation)
			return unknown(); // TODO: Annotations
		else if (target instanceof MemberSelectExpressionModel memberSelectExpression)
			return resolveMemberSelection(memberSelectExpression);
		else if (target instanceof MethodInvocationExpressionModel methodInvocationExpressionModel)
			return resolveMethodReturnType(methodInvocationExpressionModel);
		else if (target instanceof NewClassExpressionModel newClass)
			return resolveImportedDotName(newClass);
		else if (target instanceof NamedModel named)
			return resolveNameUsage(named);
		else if (target instanceof TypeModel type)
			return resolveType(type);
		else if (target instanceof CastExpressionModel cast)
			return resolve(cast.getType());
		else if (target instanceof ModifiersModel)
			return resolve(target.getParent());
		else if (target instanceof LiteralExpressionModel literal)
			return resolveLiteral(literal);
		else if (target instanceof ParenthesizedExpressionModel parenthesizedExpression)
			return parenthesizedExpression.getExpression().resolve(this);
		else if (target instanceof BinaryExpressionModel binaryExpression)
			return resolveBinaryExpression(binaryExpression);

		return unknown();
	}

	@Nonnull
	private Resolution resolveNameUsage(@Nonnull NamedModel named) {
		Model parent = named.getParent();

		if (parent instanceof ClassModel
				|| parent instanceof ImplementsModel
				|| parent instanceof CastExpressionModel
				|| parent instanceof ThrowStatementModel)
			// The named model is used in a context where it can only be a dot-name.
			return resolveImportedDotName(named);
		else if (parent instanceof InstanceofExpressionModel instanceOf
				&& instanceOf.getType() == named)
			// Only solve as a dot-name if the name is the instanceof expression's targeted type.
			// If it's the expression portion (the thing being checked) we don't want to handle that as a dot-name.
			return resolveImportedDotName(named);
		else if (parent instanceof NewClassExpressionModel newExpr
				&& newExpr.getIdentifier() == named)
			// The named model is the identifier of a 'new T()' expression so resolve as T.
			return resolveImportedDotName(newExpr);
		else if (parent instanceof TypeModel parentType)
			// The named model is part of a type, so resolve the type.
			return resolveType(parentType);
		else if (parent instanceof PackageModel parentPackage)
			// The named model is part of the package declaration.
			return resolvePackageModel(parentPackage);
		else if (parent instanceof MemberSelectExpressionModel) {
			// Member selection can be:
			//  ClassName.staticMethod() --> We want to do dot-name resolution.
			//  variable.virtualMethod() --> We want to resolve the type of 'variable' and look for the member in there.
			//   - Variable resolving is handled by fallthrough of else-if handling further below.
			Resolution resolution = resolveImportedDotName(named);
			if (!resolution.isUnknown())
				return resolution;
		} else if (parent instanceof MethodInvocationExpressionModel methodInvocation
				&& named == methodInvocation.getMethodSelect())
			// The named model is the method name.
			return resolveMember(methodInvocation);

		String name = named.getName();

		// Try looking for variables defined in the method.
		Model containingMethod = named.getParentOfType(MethodModel.class);
		if (containingMethod != null) {
			// TODO: This isn't technically correct as multiple scopes in the method can define
			//   variables of the same name, but with different types.
			//  So we'll want a walk up one scope at a time instead of just jumping to the root method
			//   and looking at all defined variables.
			//  Each scope step up we take, we still want to ensure its variables flow into the scope where
			//   our named model exists within. Consider "if (!(foo instanceof String bar)) { ... }" where the 'bar'
			//   is actually only accessible in a "else { ... }" block or following the "if" block if the block returns.
			List<VariableModel> variables = containingMethod.getRecursiveChildrenOfType(VariableModel.class);
			for (VariableModel variable : variables) {
				if (variable.getName().equals(name)) {
					Resolution resolution = resolveType(variable.getType());
					if (!resolution.isUnknown())
						return resolution;
				}
			}
		}

		// Try looking for fields defined in the class.
		//  - If it's an inner class there will be more parents of the class model type,
		//    so we'll want to iterate on the containing class if nothing is found in this one
		//  - Fields can be in this class, or any parent
		Resolution resolution = resolveMemberByNameInModel(named, named.getName(), MemberTarget.FIELDS);
		if (!resolution.isUnknown())
			return resolution;

		return unknown();
	}

	@Nonnull
	private Resolution resolveDotName(@Nonnull String name) {
		name = name.replace('.', '/');
		Resolution resolution = ofClass(pool, name);
		while (resolution.isUnknown() && name.indexOf('/') >= 0) {
			int lastSlash = name.lastIndexOf('/');
			String tail = name.substring(lastSlash + 1);
			name = name.substring(0, lastSlash) + '$' + tail;
		}
		return resolution;
	}

	@Nonnull
	private Resolution resolveImportedDotName(@Nonnull NamedModel extendsModel) {
		return resolveImportedDotName(extendsModel.getName());
	}

	@Nonnull
	private Resolution resolveImportedDotName(@Nonnull String name) {
		// If it is a qualified name, just do a dot-name lookup.
		if (name.indexOf('.') > 0)
			return resolveDotName(name);

		// Otherwise look for a name in the imports that match.
		for (Map.Entry<String, ClassEntry> importEntry : importedTypes.entrySet())
			if (importEntry.getKey().endsWith('/' + name))
				return ofClass(importEntry.getValue());

		return unknown();
	}

	@Nonnull
	private Resolution resolveType(@Nonnull TypeModel type) {
		var kind = type.getKind();
		if (kind == TypeModel.Kind.PRIMITIVE
				&& type instanceof TypeModel.Primitive primitiveType) {
			return switch (primitiveType.getPrimitiveKind()) {
				case BOOLEAN -> ofPrimitive("Z");
				case BYTE -> ofPrimitive("B");
				case SHORT -> ofPrimitive("S");
				case INT -> ofPrimitive("I");
				case LONG -> ofPrimitive("J");
				case CHAR -> ofPrimitive("C");
				case FLOAT -> ofPrimitive("F");
				case DOUBLE -> ofPrimitive("D");
				case VOID -> ofPrimitive("V");
				default -> unknown();
			};
		} else if (kind == TypeModel.Kind.OBJECT || kind == TypeModel.Kind.PARAMETERIZED) {
			return resolveAsIdentifier(type.getIdentifier());
		} else if (kind == TypeModel.Kind.ARRAY
				&& type instanceof TypeModel.Array arrayType) {
			Model elementType = arrayType.getRootModel();
			Resolution elementResolution = resolveAsIdentifier(elementType);
			if (elementResolution instanceof DescribableResolution describableElementResolution)
				return ofArray(describableElementResolution, arrayType.getDimensions());
		}

		return unknown();
	}

	@Nonnull
	private Resolution resolveAsIdentifier(@Nonnull Model identifier) {
		if (identifier instanceof TypeModel typeIdentifier)
			return resolveType(typeIdentifier);
		else if (identifier instanceof NamedModel named)
			return resolveImportedDotName(named.getName());
		return unknown();
	}

	@Nonnull
	private Resolution resolvePackageModel(@Nonnull PackageModel pkg) {
		String packageName = pkg.isDefaultPackage() ? null : pkg.getName().replace('.', '/');
		return (PackageResolution) () -> packageName;
	}

	@Nonnull
	private Resolution resolveImportModel(@Nonnull ImportModel imp) {
		String name = imp.getName();

		if (imp.isStatic()) {
			// Split the import name into 'owner:member'
			int lastDot = name.lastIndexOf('.');
			String className = name.substring(0, lastDot);
			String memberName = name.substring(lastDot + 1);

			// If the class can be resolved, yield the methods matching the given member name
			if (resolveDotName(className) instanceof ClassResolution declaringClassResolution) {
				if (memberName.lastIndexOf('*') >= 0) {
					// Star import, so all members of the class should be returned.
					List<ClassMemberPair> memberEntries = new ArrayList<>();
					declaringClassResolution.getClassEntry().visitHierarchy(owner -> {
						memberEntries.addAll(owner.memberStream()
								.filter(e -> !e.isPrivate() && e.isStatic())
								.map(e -> new ClassMemberPair(owner, e))
								.toList());
					});
					return ofMembers(memberEntries);
				} else {
					// Specific name import, so only members with the same name should be returned.
					List<ClassMemberPair> memberEntries = new ArrayList<>();
					declaringClassResolution.getClassEntry().visitHierarchy(owner -> {
						memberEntries.addAll(owner.memberStream()
								.filter(e -> !e.isPrivate() && e.isStatic() && e.getName().equals(memberName))
								.map(e -> new ClassMemberPair(owner, e))
								.toList());
					});
					return ofMembers(memberEntries);
				}
			}
			return unknown();
		}

		// If we're importing a whole package, we need a multi-class resolution for all
		// the classes in that package.
		if (name.endsWith(".*")) {
			// Technically you could do "com.example.OuterClass.*" but that shouldn't occur frequently enough
			// to bother supporting it here.
			String packageName = name.substring(0, name.lastIndexOf(".*")).replace('.', '/');
			return ofClasses(pool.getClassesInPackage(packageName));
		}

		return resolveDotName(name);
	}

	@Nonnull
	private Resolution resolveClassModel(@Nonnull ClassModel clazz) {
		String name = clazz.getName();
		if (unit.getPackage().isDefaultPackage())
			return ofClass(pool, name);
		return resolveDotName(unit.getPackage().getName() + '.' + name);
	}

	@Nonnull
	private Resolution resolveFieldModel(@Nonnull ClassModel definingClass, @Nonnull VariableModel field) {
		// Skip if parent context cannot be resolved.
		if (!(definingClass.resolve(this) instanceof ClassResolution resolvedDefiningClass))
			return unknown();

		// Check and see if we can take a shortcut by just doing a name lookup.
		String fieldName = field.getName();
		ClassEntry definingClassEntry = resolvedDefiningClass.getClassEntry();
		if (resolveFieldByNameInClass(definingClassEntry, fieldName, null) instanceof FieldResolution resolution)
			return resolution;

		// Can't take a shortcut, we need to resolve the descriptor then look up with that.
		if (field.getType().resolve(this) instanceof DescribableResolution resolvedType)
			return ofField(definingClassEntry, fieldName, resolvedType.getDescribableEntry().getDescriptor());

		// Cannot resolve field.
		return unknown();
	}

	@Nonnull
	private Resolution resolveMethodModel(@Nonnull MethodModel method) {
		// Skip if parent context cannot be resolved.
		if (!(method.getParent() instanceof ClassModel definingClass))
			return unknown();
		if (!(definingClass.resolve(this) instanceof ClassResolution resolvedDefiningClass))
			return unknown();

		// Check and see if we can take a shortcut by just doing a name lookup.
		String methodName = method.getName();
		ClassEntry definingClassEntry = resolvedDefiningClass.getClassEntry();
		if (resolveMethodByNameInClass(definingClassEntry, methodName) instanceof MethodResolution resolution)
			return resolution;

		// Can't take a shortcut, we need to resolve the descriptor then look up with that.
		if (!(method.getReturnType().resolve(this) instanceof DescribableResolution resolvedReturnType))
			return unknown();
		List<VariableModel> parameters = method.getParameters();
		List<DescribableResolution> resolvedParameterTypes = new ArrayList<>(parameters.size());
		for (VariableModel parameter : parameters) {
			Resolution resolution = parameter.resolve(this);
			if (resolution instanceof DescribableResolution resolvedParameter)
				resolvedParameterTypes.add(resolvedParameter);
			else
				// If a parameter is not resolvable, we cannot resolve this method
				return unknown();
		}

		// Resolve by name/descriptor.
		return ofMethod(definingClassEntry, methodName, resolvedReturnType.getDescribableEntry(),
				resolvedParameterTypes.stream().map(DescribableResolution::getDescribableEntry).toList());
	}

	@Nonnull
	private Resolution resolveFieldByNameInClass(@Nonnull ClassEntry classEntry, @Nonnull String fieldName,
	                                             @Nullable DescribableEntry typeEntryHint) {
		// Edge case for implicit "this" class variable.
		if (fieldName.equals("this"))
			return ofClass(classEntry);

		// Check if the field is declared in this class, and is unique in the hierarchy in terms of signature.
		List<FieldEntry> fieldsByName = classEntry.getFieldsByName(fieldName);
		if (fieldsByName.size() == 1)
			return ofField(classEntry, fieldsByName.get(0));

		// Check in super-type.
		if (classEntry.getSuperEntry() != null
				&& resolveFieldByNameInClass(classEntry.getSuperEntry(), fieldName, typeEntryHint) instanceof FieldResolution resolution)
			return resolution;

		// Check in interfaces.
		for (ClassEntry implementedEntry : classEntry.getImplementedEntries())
			if (resolveFieldByNameInClass(implementedEntry, fieldName, typeEntryHint) instanceof FieldResolution resolution)
				return resolution;

		return unknown();
	}

	@Nonnull
	private Resolution resolveMethodByNameInClass(@Nonnull ClassEntry classEntry, @Nonnull String methodName) {
		return resolveMethodByNameInClass(classEntry, methodName, null, null);
	}

	private Resolution resolveMethodByNameInClass(@Nonnull ClassEntry classEntry, @Nonnull String methodName,
	                                              @Nullable DescribableEntry returnTypeEntry,
	                                              @Nullable List<DescribableEntry> argumentTypeEntries) {
		// Check if the method is declared in this class.
		//  - Only one match by name   --> match
		//  - Multiple matches by name --> filter by matching signature --> match
		List<MethodEntry> methodsByName = classEntry.getMethodsByName(methodName);
		if (methodsByName.size() == 1)
			return ofMethod(classEntry, methodsByName.get(0));
		if (methodsByName.size() > 1 && (returnTypeEntry != null || argumentTypeEntries != null)) {
			// Try and prune candidates by filtering against presumed return/argument types.
			for (int i = methodsByName.size() - 1; i >= 0; i--) {
				MethodEntry methodEntry = methodsByName.get(i);

				// Prune method candidates with mismatching return types.
				if (returnTypeEntry != null) {
					DescribableEntry describableReturn = pool.getDescribable(methodEntry.getReturnDescriptor());
					if (describableReturn != null && !describableReturn.isAssignableFrom(returnTypeEntry)) {
						methodsByName.remove(methodEntry);
						continue;
					}
				}

				// Prune method candidates with mismatching argument types.
				if (argumentTypeEntries != null) {
					List<String> argumentDescriptors = methodEntry.getParameterDescriptors();
					int hintedArgCount = argumentTypeEntries.size();
					int actualArgCount = argumentDescriptors.size();
					int maxArgToCheck;
					if (methodEntry.isVarargs()) {
						// For vararg methods we only want to check the args up to the varargs parameter
						// in the loop further below. We will handle variable arg type checking specially here.
						maxArgToCheck = actualArgCount - 1;
						if (hintedArgCount < maxArgToCheck) {
							// If the hinted arg count more than one less than the actual arg count, that means we hit a situation like:
							//  Actual:  A, B, C, V...
							//  Hinted:  ?, ?
							// In this case, it cannot possibly be a match.
							methodsByName.remove(methodEntry);
							break;
						}

						// All hinted variable arguments must be assignable to the actual variable argument's element type.
						String varargParameterDescriptor = argumentDescriptors.get(argumentDescriptors.size() - 1);
						if (varargParameterDescriptor.charAt(0) == '[')
							varargParameterDescriptor = varargParameterDescriptor.substring(1);
						DescribableEntry varargElementType = pool.getDescribable(varargParameterDescriptor);
						if (varargElementType != null) {
							boolean methodRemoved = false;
							for (int j = maxArgToCheck; j < hintedArgCount; j++) {
								if (!varargElementType.isAssignableFrom(argumentTypeEntries.get(j))) {
									methodRemoved = true;
									methodsByName.remove(methodEntry);
									break;
								}
							}
							if (methodRemoved)
								break;
						}
					} else {
						// Not a vararg method, we want to check against all arguments.
						maxArgToCheck = actualArgCount;

						// If the argument count does not match, it is not a valid consideration.
						if (hintedArgCount != actualArgCount) {
							methodsByName.remove(methodEntry);
							continue;
						}
					}
					for (int j = 0; j < maxArgToCheck; j++) {
						String parameterDescriptor = argumentDescriptors.get(j);
						DescribableEntry describableParameter = pool.getDescribable(parameterDescriptor);
						if (describableParameter != null && !describableParameter.isAssignableFrom(argumentTypeEntries.get(j))) {
							methodsByName.remove(methodEntry);
							break;
						}
					}
				}
			}

			// Check again after pruning if there is only a single candidate.
			if (methodsByName.size() == 1)
				return ofMethod(classEntry, methodsByName.get(0));

			// Check and see if there is an exact descriptor match.
			//  TODO: Case where the returnValue but not args are given, case where both are given
			if (argumentTypeEntries != null) {
				String argsDesc = "(" + argumentTypeEntries.stream().map(DescribableEntry::getDescriptor).collect(Collectors.joining("")) + ")";
				methodsByName = methodsByName.stream()
						.filter(e -> e.getDescriptor().startsWith(argsDesc))
						.collect(Collectors.toList());
				if (methodsByName.size() == 1)
					return ofMethod(classEntry, methodsByName.get(0));
			}
		}

		// Check in super-type.
		if (classEntry.getSuperEntry() != null
				&& resolveMethodByNameInClass(classEntry.getSuperEntry(), methodName, returnTypeEntry, argumentTypeEntries) instanceof MethodResolution resolution)
			return resolution;

		// Check in interfaces.
		for (ClassEntry implementedEntry : classEntry.getImplementedEntries())
			if (resolveMethodByNameInClass(implementedEntry, methodName, returnTypeEntry, argumentTypeEntries) instanceof MethodResolution resolution)
				return resolution;

		return unknown();
	}

	@Nonnull
	private Resolution resolveMemberByNameInModel(@Nonnull Model origin, @Nonnull String name, @Nonnull MemberTarget target) {
		boolean isFieldsTarget = target == MemberTarget.FIELDS;

		ClassModel classContext = origin.getParentOfType(ClassModel.class);
		boolean wasLastClassContextStatic = false;
		while (classContext != null) {
			if (resolveClassModel(classContext) instanceof ClassResolution classResolution) {
				// When we get the class entry, we want to filter it to only view static content if we are coming from
				// the context of a static inner class. If it is a non-static class or a top-level class
				// then no filtering is needed.
				ClassEntry classEntry = wasLastClassContextStatic ?
						new StaticFilteredClassEntry(classResolution.getClassEntry()) : classResolution.getClassEntry();
				Resolution resolution = isFieldsTarget ?
						resolveFieldByNameInClass(classEntry, name, null) :
						resolveMethodByNameInClass(classEntry, name, null,
								collectMethodArgumentsInParentContext(origin) /* TODO: Only lookup if needed */);
				if (!resolution.isUnknown())
					return resolution;
				wasLastClassContextStatic = classEntry.isStatic();
			}
			classContext = classContext.getParentOfType(ClassModel.class);
		}

		// Try looking for imported static members.
		String namedStaticImportPattern = '.' + name;
		for (ImportModel imp : unit.getImports()) {
			if (!imp.isStatic())
				continue;

			// Examples:
			//   import static com.foo.Utils.FIELD_NAME
			//   import static com.foo.Utils.*
			if (imp.getName().endsWith(namedStaticImportPattern) || imp.getName().endsWith(".*")) {
				Resolution importResolution = resolveImportModel(imp);
				if (isFieldsTarget) {
					// Resolve against imported fields
					if (importResolution instanceof FieldResolution fieldResolution
							&& fieldResolution.getFieldEntry().getName().equals(name)) {
						return fieldResolution;
					} else if (importResolution instanceof MultiMemberResolution multiMemberresolution) {
						for (ClassMemberPair pair : multiMemberresolution.getMemberEntries()) {
							MemberEntry memberEntry = pair.memberEntry();
							if (memberEntry.isField() && memberEntry.getName().equals(name))
								return ofMember(pair);
						}
					}
				} else {
					// Resolve against imported methods if the name origin is a method invocation
					if (origin instanceof MethodInvocationExpressionModel invocation) {
						if (importResolution instanceof MethodResolution methodResolution
								&& methodResolution.getMethodEntry().getName().equals(name)) {
							return resolveMemberInContext(ofClass(methodResolution.getOwnerEntry()), invocation, name);
						} else if (importResolution instanceof MultiMemberResolution multiMemberresolution) {
							for (ClassMemberPair pair : multiMemberresolution.getMemberEntries()) {
								MemberEntry memberEntry = pair.memberEntry();
								if (memberEntry.isMethod() && memberEntry.getName().equals(name))
									return resolveMemberInContext(ofClass(pair.ownerEntry()), invocation, name);
							}
						}
					}
				}
			}
		}

		return unknown();
	}

	@Nonnull
	private Resolution resolveStaticInitializer(@Nonnull MethodModel method) {
		// Skip if parent context cannot be resolved.
		if (!(method.getParent() instanceof ClassModel definingClass))
			return unknown();
		if (!(definingClass.resolve(this) instanceof ClassResolution resolvedDefiningClass))
			return unknown();

		// Static initializers will only be resolved in the target class.
		List<MethodEntry> initializers = resolvedDefiningClass.getClassEntry().getMethodsByName("<clinit>");
		if (initializers.isEmpty())
			return unknown();
		return ofMethod(resolvedDefiningClass.getClassEntry(), initializers.get(0));
	}

	@Nonnull
	private Resolution resolveVariableType(@Nonnull VariableModel variable) {
		TypeModel type = variable.getType();

		// If the type is 'var' then we will solve based on the assigned value expression
		if (type.getKind() == TypeModel.Kind.VAR)
			return unknown(); // TODO: Solve for variable.getValue()

		// Otherwise resolve declared type
		return resolveType(type);
	}

	@Nonnull
	private Resolution resolveMember(@Nonnull MethodInvocationExpressionModel methodInvocation) {
		AbstractExpressionModel select = methodInvocation.getMethodSelect();
		if (select instanceof MemberSelectExpressionModel memberSelect)
			// Selection is in the pattern of 'context.methodName' so solve with the context in mind.
			return resolveMemberSelection(memberSelect);
		else if (select instanceof NameExpressionModel named)
			// Selection is in the pattern of 'methodName' so solve with the containing class as context.
			return resolveMemberByNameInModel(methodInvocation, named.getName(), MemberTarget.METHODS);
		return unknown();
	}

	@Nonnull
	private Resolution resolveMemberSelection(@Nonnull MemberSelectExpressionModel memberSelect) {
		String memberName = memberSelect.getName();
		Resolution contextResolution = memberSelect.getContext().resolve(this);
		return resolveMemberInContext(contextResolution, memberSelect, memberName);
	}

	@Nonnull
	private Resolution resolveMemberInContext(@Nonnull Resolution contextResolution, @Nonnull Model origin, @Nonnull String memberName) {
		if (origin.getParent() instanceof MethodInvocationExpressionModel methodInvocation) {
			// TODO: Resolve the implied return type based on the methodInvocation's use case
			//  and use that as an additional hint to 'resolveMethodByNameInClass'
			//   - But only if necessary
			DescribableEntry returnType = null;

			// Resolve the method's arguments.
			//  TODO: Only do this if necessary
			List<DescribableEntry> describableArguments = collectMethodArgumentsInParentContext(methodInvocation);

			// Member selection is the method identifier
			if (contextResolution instanceof ClassResolution classResolution) {
				ClassEntry declaringClass = classResolution.getClassEntry();
				return resolveMethodByNameInClass(declaringClass, memberName, returnType, describableArguments);
			} else if (contextResolution instanceof MemberResolution memberResolution) {
				ClassEntry declaringClass = memberResolution.getOwnerEntry();
				return resolveMethodByNameInClass(declaringClass, memberName, returnType, describableArguments);
			}
		} else {
			if (contextResolution instanceof ClassResolution classResolution) {
				ClassEntry declaringClass = classResolution.getClassEntry();
				if (origin instanceof MethodInvocationExpressionModel) {
					// TODO: Resolve the implied return type based on the methodInvocation's use case
					//  and use that as an additional hint to 'resolveMethodByNameInClass'
					//   - But only if necessary
					DescribableEntry returnType = null;

					// Resolve the method's arguments.
					//  TODO: Only do this if necessary
					List<DescribableEntry> describableArguments = collectMethodArgumentsInParentContext(origin);

					// Member selection should be in the context of a class identifier such as:
					//  - StringConstants.TARGET_NAME
					return resolveMethodByNameInClass(declaringClass, memberName, returnType, describableArguments);
				} else {
					// TODO: Resolve the implied field type based on the use case of the selection
					//  and use that as an additional hint to 'resolveFieldByNameInClass'
					DescribableEntry usageType = null;

					// Member selection should be a field identifier in the context of a class identifier such as:
					//  - StringConstants.TARGET_NAME
					return resolveFieldByNameInClass(declaringClass, memberName, usageType);
				}
			} else if (contextResolution instanceof FieldResolution fieldResolution) {
				// TODO: Resolve the implied field type based on the use case of the selection
				//  and use that as an additional hint to 'resolveFieldByNameInClass'
				DescribableEntry usageType = null;

				// The identifier is in the context of another member identifier such as:
				//  - someField.targetName
				DescribableEntry fieldType = pool.getDescribable(fieldResolution.getDescribableEntry().getDescriptor());
				if (fieldType instanceof ClassEntry fieldClassType) {
					return resolveFieldByNameInClass(fieldClassType, memberName, usageType);
				} else if (fieldType instanceof ArrayEntry) {
					// Special case handling for arrays
					if (memberName.equals("length"))
						return ofPrimitive(INT);
					return resolveFieldByNameInClass(Objects.requireNonNull(pool.getClass("java/lang/Object")), memberName, usageType);
				}
			} else if (contextResolution instanceof ArrayResolution) {
				// The identifier is in the context of another member identifier representing an array variable such as:
				//  - args.length
				if (memberName.equals("length"))
					return ofPrimitive(INT);
			}
		}

		return unknown();
	}

	@Nonnull
	private Resolution resolveMethodReturnType(@Nonnull MethodInvocationExpressionModel methodInvocation) {
		if (resolveMember(methodInvocation) instanceof MethodResolution resolvedInvocation) {
			DescribableEntry returnValueEntry = pool.getDescribable(resolvedInvocation.getMethodEntry().getReturnDescriptor());
			if (returnValueEntry instanceof ArrayEntry array)
				return ofArray(array);
			else if (returnValueEntry instanceof PrimitiveEntry primitive)
				return ofPrimitive(primitive);
			else if (returnValueEntry instanceof ClassEntry clazz)
				return ofClass(clazz);
		}

		return unknown();
	}

	@Nonnull
	private Resolution resolveBinaryExpression(@Nonnull BinaryExpressionModel binary) {
		return switch (binary.getOperator()) {
			case PLUS, MINUS, MULTIPLY, DIVIDE, REMAINDER,
					BIT_OR, BIT_AND, BIT_XOR,
					SHIFT_LEFT, SHIFT_RIGHT, SHIFT_RIGHT_UNSIGNED ->
					ofPrimitive(INT); // TODO: Can be other wider types too based on contents of left/right models
			case EQUALS, NOT_EQUALS,
					CONDITIONAL_OR, CONDITIONAL_AND,
					RELATION_LESS, RELATION_GREATER,
					RELATION_LESS_EQUAL, RELATION_GREATER_EQUAL,
					RELATION_INSTANCEOF -> ofPrimitive(BOOLEAN);
			case UNKNOWN -> unknown();
		};
	}

	@Nonnull
	private Resolution resolveLiteral(@Nonnull LiteralExpressionModel literal) {
		return switch (literal.getKind()) {
			case INT -> ofPrimitive(INT);
			case LONG -> ofPrimitive(LONG);
			case FLOAT -> ofPrimitive(FLOAT);
			case DOUBLE -> ofPrimitive(DOUBLE);
			case BOOLEAN -> ofPrimitive(BOOLEAN);
			case CHAR -> ofPrimitive(CHAR);
			case STRING -> ofClass(pool, "java/lang/String");
			case NULL -> nul();
			default -> unknown();
		};
	}

	@Nullable
	private List<DescribableEntry> collectMethodArgumentsInParentContext(@Nonnull Model origin) {
		MethodInvocationExpressionModel methodInvocation = origin instanceof MethodInvocationExpressionModel invoke
				? invoke : origin.getParentOfType(MethodInvocationExpressionModel.class);
		if (methodInvocation == null)
			return null;

		List<AbstractExpressionModel> arguments = methodInvocation.getArguments();
		List<DescribableEntry> describableArguments = arguments.isEmpty() ? Collections.emptyList() : new ArrayList<>(arguments.size());
		for (AbstractExpressionModel argument : arguments) {
			Resolution resolution = argument.resolve(this);
			if (resolution instanceof DescribableResolution describableResolution) {
				DescribableEntry argumentDescribableEntry = describableResolution.getDescribableEntry();
				if (argumentDescribableEntry instanceof FieldEntry)
					// Because a field entry is describable, but does not support assignable checking we
					// need to re-interpret the descriptor with contents from the entry-pool.
					describableArguments.add(pool.getDescribable(argumentDescribableEntry.getDescriptor()));
				else
					describableArguments.add(argumentDescribableEntry);
			} else {
				// If any of the arguments cannot be described, then we will treat it as if
				// we don't know anything about the arguments at all.
				return null;
			}
		}

		return describableArguments;
	}

	private enum MemberTarget {
		FIELDS, METHODS
	}
}
