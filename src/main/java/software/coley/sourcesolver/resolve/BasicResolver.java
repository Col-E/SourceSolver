package software.coley.sourcesolver.resolve;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AnnotationArgumentModel;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.ArrayDeclarationExpressionModel;
import software.coley.sourcesolver.model.AssignmentExpressionModel;
import software.coley.sourcesolver.model.BinaryExpressionModel;
import software.coley.sourcesolver.model.CastExpressionModel;
import software.coley.sourcesolver.model.ClassModel;
import software.coley.sourcesolver.model.CompilationUnitModel;
import software.coley.sourcesolver.model.ImplementsModel;
import software.coley.sourcesolver.model.ImportModel;
import software.coley.sourcesolver.model.InstanceofExpressionModel;
import software.coley.sourcesolver.model.LambdaExpressionModel;
import software.coley.sourcesolver.model.LiteralExpressionModel;
import software.coley.sourcesolver.model.MemberSelectExpressionModel;
import software.coley.sourcesolver.model.MethodInvocationExpressionModel;
import software.coley.sourcesolver.model.MethodModel;
import software.coley.sourcesolver.model.MethodReferenceExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.model.NamedModel;
import software.coley.sourcesolver.model.NewClassExpressionModel;
import software.coley.sourcesolver.model.PackageModel;
import software.coley.sourcesolver.model.ParenthesizedExpressionModel;
import software.coley.sourcesolver.model.PermitsModel;
import software.coley.sourcesolver.model.ScopeLookup;
import software.coley.sourcesolver.model.SwitchExpressionModel;
import software.coley.sourcesolver.model.ThrowStatementModel;
import software.coley.sourcesolver.model.TypeModel;
import software.coley.sourcesolver.model.TypeParameterModel;
import software.coley.sourcesolver.model.VariableModel;
import software.coley.sourcesolver.model.YieldStatementModel;
import software.coley.sourcesolver.resolve.entry.ArrayEntry;
import software.coley.sourcesolver.resolve.entry.BasicMethodEntry;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.ClassMemberPair;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.EntryPool;
import software.coley.sourcesolver.resolve.entry.FieldEntry;
import software.coley.sourcesolver.resolve.entry.MemberEntry;
import software.coley.sourcesolver.resolve.entry.MethodEntry;
import software.coley.sourcesolver.resolve.entry.MultiClassEntry;
import software.coley.sourcesolver.resolve.entry.NullEntry;
import software.coley.sourcesolver.resolve.entry.PrimitiveEntry;
import software.coley.sourcesolver.resolve.entry.StaticFilteredClassEntry;
import software.coley.sourcesolver.resolve.generic.GenericType;
import software.coley.sourcesolver.resolve.generic.GenericTypeParameter;
import software.coley.sourcesolver.resolve.generic.GenericTypes;
import software.coley.sourcesolver.resolve.result.ArrayResolution;
import software.coley.sourcesolver.resolve.result.ClassResolution;
import software.coley.sourcesolver.resolve.result.DescribableResolution;
import software.coley.sourcesolver.resolve.result.FieldResolution;
import software.coley.sourcesolver.resolve.result.MethodResolution;
import software.coley.sourcesolver.resolve.result.MultiClassResolution;
import software.coley.sourcesolver.resolve.result.MultiMemberResolution;
import software.coley.sourcesolver.resolve.result.NullResolution;
import software.coley.sourcesolver.resolve.result.PackageResolution;
import software.coley.sourcesolver.resolve.result.PrimitiveResolution;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.resolve.result.Resolutions;
import software.coley.sourcesolver.resolve.result.ThrowingResolution;
import software.coley.sourcesolver.resolve.result.VariableResolution;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.resolve.entry.PrimitiveEntry.*;
import static software.coley.sourcesolver.resolve.result.Resolutions.MergeOp.ADDITION_OR_CONCAT;
import static software.coley.sourcesolver.resolve.result.Resolutions.*;

/**
 * Base resolver implementation.
 *
 * @author Matt Coley
 */
public class BasicResolver implements Resolver {
	// Shared stub for static initializer methods.
	// This is used to resolve references to the <clinit> method when users don't provide a class entry model that contains them.
	private static final MethodEntry STATIC_INIT_STUB = new BasicMethodEntry("<clinit>", "()V", Modifier.STATIC, GenericTypes.ofPrimitive(VOID), List.of());
	private final Map<String, ClassEntry> importedTypes;
	private final CompilationUnitModel unit;
	private final EntryPool pool;
	private final ClassEntry jlObjectEntry, jlClassEntry;
	private final Set<Model> usageInferenceInProgress = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<MethodInvocationExpressionModel, Set<Integer>> argumentInferenceInProgress = new IdentityHashMap<>();
	private Map<ClassModel, ClassEntry> externallyResolvedClassEntries;

	/**
	 * @param unit
	 * 		Root element model to resolve contents of.
	 * @param pool
	 * 		Pool to access class metadata.
	 */
	public BasicResolver(@Nonnull CompilationUnitModel unit, @Nonnull EntryPool pool) {
		this.unit = unit;
		this.pool = pool;

		jlObjectEntry = Objects.requireNonNull(pool.getClass("java/lang/Object"), "EntryPool does not contain 'java/lang/Object'");
		jlClassEntry = Objects.requireNonNull(pool.getClass("java/lang/Class"), "EntryPool does not contain 'java/lang/Class'");

		importedTypes = Collections.unmodifiableMap(populateImports());
	}

	/**
	 * @return Backing entry pool containing definitions of classes to resolve to.
	 */
	@Nonnull
	protected EntryPool getPool() {
		return pool;
	}

	/**
	 * @return Backing compilation unit to resolve contents of.
	 */
	@Nonnull
	protected CompilationUnitModel getUnit() {
		return unit;
	}

	/**
	 * @return Map of internal names to class entries for all imported <i>(implicit and explicit)</i> classes in the compilation unit.
	 */
	@Nonnull
	protected Map<String, ClassEntry> populateImports() {
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
	public Resolution resolveAt(int position, @Nullable Model target) {
		if (target != null)
			return resolve(target);

		// Resolve off of the deepest model so that it is aware of the results and can cache them.
		return unit.getDeepestChildAtPosition(position).resolve(this);
	}

	@Nonnull
	@Override
	public Resolution resolveReferenceAt(@Nonnull String name, int position) {
		String trimmedName = name.trim();
		if (trimmedName.isEmpty())
			return unknown();

		// Prefer exact source-model matches first so callers get the same resolution they would
		// have received from a normal AST lookup when the fragment already exists in the unit.
		Resolution sourceResolution = resolveSourceFragment(trimmedName, position);
		if (!sourceResolution.isUnknown())
			return sourceResolution;

		// Handle the implicit receiver keywords directly because they are contextual and do not
		// correspond to normal imported or locally-declared names.
		if ("this".equals(trimmedName))
			return resolveEnclosingClass(position);
		if ("super".equals(trimmedName)) {
			Resolution enclosingClass = resolveEnclosingClass(position);
			if (enclosingClass instanceof ClassResolution classResolution) {
				ClassEntry superEntry = classResolution.getClassEntry().getSuperEntry();
				if (superEntry != null)
					return ofClass(superEntry);
			}
			return unknown();
		}

		// Check for qualified name access.
		if (trimmedName.indexOf('.') >= 0) {
			Resolution typeResolution = resolveNameAsQualifiedOrImported(trimmedName);
			if (!typeResolution.isUnknown())
				return typeResolution;
			return resolveAsInnerClass(trimmedName);
		}

		int resolvedPosition = clampPosition(position);

		// Unqualified names are checked in roughly the same order a user would reason about them
		// in source: local scope, enclosing class members, then type parameters and visible types.
		VariableModel variable = ScopeLookup.findVisibleVariable(unit, resolvedPosition, trimmedName);
		if (variable != null) {
			Resolution variableResolution = variable.resolve(this);
			if (!variableResolution.isUnknown())
				return variableResolution;
		}

		Resolution fieldResolution = resolveFieldFromEnclosingClasses(trimmedName, resolvedPosition);
		if (!fieldResolution.isUnknown())
			return fieldResolution;

		Resolution typeParameterResolution = resolveTypeParameterAt(trimmedName, resolvedPosition);
		if (!typeParameterResolution.isUnknown())
			return typeParameterResolution;

		Resolution typeResolution = resolveNameAsQualifiedOrImported(trimmedName);
		if (!typeResolution.isUnknown())
			return typeResolution;
		return resolveAsInnerClass(trimmedName);
	}

	@Nonnull
	@Override
	public Resolution resolveFragmentAt(@Nonnull String text, int position) {
		String trimmedText = text.trim();
		if (trimmedText.isEmpty())
			return unknown();

		// If the fragment exactly matches an AST-backed model, trust the normal resolver path first.
		Resolution sourceResolution = resolveSourceFragment(trimmedText, position);
		if (!sourceResolution.isUnknown())
			return sourceResolution;

		// Simple identifiers and qualified type names are handled by the reference resolver.
		Resolution referenceResolution = resolveReferenceAt(trimmedText, position);
		if (!referenceResolution.isUnknown())
			return referenceResolution;

		// Tooling often has only partial source text for a receiver chain, so fall back to parsing
		// the trailing operation and resolving the base fragment recursively.
		AccessFragment methodInvocation = parseTrailingZeroArgInvocation(trimmedText);
		if (methodInvocation != null) {
			Resolution baseResolution = resolveFragmentAt(methodInvocation.baseText(),
					adjustPositionForBase(position, trimmedText, methodInvocation.baseText()));
			if (!baseResolution.isUnknown()) {
				Resolution methodResolution = resolveMethodInContext(baseResolution, methodInvocation.memberName(), null, List.of());
				if (!methodResolution.isUnknown())
					return Resolutions.toValueTypeResolution(methodResolution);
			}
		}

		AccessFragment memberSelection = parseTrailingMemberSelection(trimmedText);
		if (memberSelection != null) {
			Resolution baseResolution = resolveFragmentAt(memberSelection.baseText(),
					adjustPositionForBase(position, trimmedText, memberSelection.baseText()));
			if (!baseResolution.isUnknown())
				return resolveFieldInContext(baseResolution, memberSelection.memberName());
		}

		return unknown();
	}

	@Nonnull
	@Override
	public Resolution resolveFieldInContext(@Nonnull Resolution contextResolution,
	                                        @Nonnull String fieldName,
	                                        @Nullable DescribableEntry typeHint) {
		// Member selection should be a field identifier in the context of a class identifier such as:
		//  - StringConstants.TARGET_NAME
		if (contextResolution instanceof ClassResolution classResolution) {
			return resolveFieldByNameInClass(Resolutions.getResolvedClassType(classResolution), fieldName, typeHint);
		} else if (contextResolution instanceof FieldResolution fieldResolution) {
			// The identifier is in the context of another member identifier such as:
			//  - someField.targetName
			GenericType describableFieldType = Resolutions.getResolvedFieldGenericType(fieldResolution);
			GenericType usableFieldType = GenericTypes.toUsableType(describableFieldType, jlObjectEntry);
			if (usableFieldType instanceof GenericType.ClassType declaringClass)
				return resolveFieldByNameInClass(declaringClass, fieldName, typeHint);
			else if (usableFieldType != null && usableFieldType.asDescribable() instanceof ArrayEntry) {
				// The identifier is in the context of another member identifier representing an array variable such as:
				//  - args.length
				if (fieldName.equals("length"))
					return ofPrimitive(INT);
				return resolveFieldByNameInClass(jlObjectEntry, fieldName, typeHint);
			}
		} else if (contextResolution instanceof VariableResolution variableResolution) {
			GenericType variableType = Resolutions.getResolvedVariableGenericType(variableResolution);
			GenericType usableVariableType = GenericTypes.toUsableType(variableType, jlObjectEntry);
			if (usableVariableType instanceof GenericType.ClassType declaringClass)
				return resolveFieldByNameInClass(declaringClass, fieldName, typeHint);
			else if (usableVariableType != null && usableVariableType.asDescribable() instanceof ArrayEntry) {
				// The identifier is in the context of an array variable such as:
				//  - args.length
				if (fieldName.equals("length"))
					return ofPrimitive(INT);
				return resolveFieldByNameInClass(jlObjectEntry, fieldName, typeHint);
			}
		} else if (contextResolution instanceof MethodResolution methodResolution) {
			GenericType returnType = Resolutions.getResolvedMethodReturnGenericType(methodResolution);
			GenericType usableReturnType = GenericTypes.toUsableType(returnType, jlObjectEntry);
			if (usableReturnType instanceof GenericType.ClassType declaringClass)
				return resolveFieldByNameInClass(declaringClass, fieldName, typeHint);
		} else if (contextResolution instanceof ArrayResolution) {
			// The identifier is in the context of another member identifier representing an array variable such as:
			//  - args.length
			if (fieldName.equals("length"))
				return ofPrimitive(INT);
			return resolveFieldByNameInClass(jlObjectEntry, fieldName, typeHint);
		}

		return unknown();
	}

	@Nonnull
	@Override
	public Resolution resolveMethodInContext(@Nonnull Resolution contextResolution,
	                                         @Nonnull String methodName,
	                                         @Nullable DescribableEntry returnTypeHint,
	                                         @Nullable List<? extends DescribableEntry> argumentTypeHints) {
		List<GenericType> genericArguments = toGenericTypeHints(argumentTypeHints);
		List<DescribableEntry> describableArguments = argumentTypeHints == null ? null : List.copyOf(argumentTypeHints);
		if (contextResolution instanceof ClassResolution classResolution) {
			return resolveMethodByNameInClass(Resolutions.getResolvedClassType(classResolution), methodName,
					rawGenericType(returnTypeHint), genericArguments, describableArguments);
		} else if (contextResolution instanceof FieldResolution fieldResolution) {
			GenericType fieldType = Resolutions.getResolvedFieldGenericType(fieldResolution);
			GenericType usableFieldType = GenericTypes.toUsableType(fieldType, jlObjectEntry);
			if (usableFieldType instanceof GenericType.ClassType declaringClass)
				return resolveMethodByNameInClass(declaringClass, methodName,
						rawGenericType(returnTypeHint), genericArguments, describableArguments);
		} else if (contextResolution instanceof VariableResolution variableResolution) {
			GenericType variableType = Resolutions.getResolvedVariableGenericType(variableResolution);
			GenericType usableVariableType = GenericTypes.toUsableType(variableType, jlObjectEntry);
			if (usableVariableType instanceof GenericType.ClassType declaringClass)
				return resolveMethodByNameInClass(declaringClass, methodName,
						rawGenericType(returnTypeHint), genericArguments, describableArguments);
			else if (usableVariableType != null && usableVariableType.asDescribable() instanceof ArrayEntry)
				return resolveMethodByNameInClass(GenericTypes.ofClass(jlObjectEntry), methodName,
						rawGenericType(returnTypeHint), genericArguments, describableArguments);
		} else if (contextResolution instanceof MethodResolution methodResolution) {
			GenericType methodReturnType = Resolutions.getResolvedMethodReturnGenericType(methodResolution);
			GenericType usableReturnType = GenericTypes.toUsableType(methodReturnType, jlObjectEntry);
			if (usableReturnType instanceof GenericType.ClassType declaringClass)
				return resolveMethodByNameInClass(declaringClass, methodName,
						rawGenericType(returnTypeHint), genericArguments, describableArguments);
		} else if (contextResolution instanceof ArrayResolution) {
			return resolveMethodByNameInClass(GenericTypes.ofClass(jlObjectEntry), methodName,
					rawGenericType(returnTypeHint), genericArguments, describableArguments);
		}

		return unknown();
	}

	@Override
	public void setDeclaredClass(@Nonnull ClassModel declaredClassModel,
	                             @Nullable ClassEntry declaredClassEntry) {
		if (externallyResolvedClassEntries == null)
			externallyResolvedClassEntries = new IdentityHashMap<>();
		externallyResolvedClassEntries.put(declaredClassModel, declaredClassEntry);
	}

	@Nonnull
	protected Resolution resolve(@Nonnull Model target) {
		return switch (target) {
			case ClassModel clazz -> resolveClassModel(clazz);
			case MethodModel method -> resolveMethodModel(method);
			case VariableModel variable ->
					target.getParent() instanceof ClassModel declaringClass ? resolveFieldModel(declaringClass, variable) : resolveVariableModel(variable);
			case PackageModel pkg -> resolvePackageModel(pkg);
			case ImportModel imp -> resolveImportModel(imp);
			case ModifiersModel modifiers when modifiers.getParent() instanceof MethodModel method && method.getName().equals("<clinit>") ->
					resolveStaticInitializer(method);
			case AnnotationArgumentModel argument -> resolveAnnotationArgument(argument);
			case AnnotationExpressionModel annotation -> annotation.getNameModel().resolve(this);
			case MemberSelectExpressionModel memberSelectExpression -> {
				Model parent = memberSelectExpression.getParent();
				if (parent instanceof NewClassExpressionModel || parent instanceof TypeModel)
					yield resolveNamed(memberSelectExpression);
				yield resolveMemberSelection(memberSelectExpression);
			}
			case MethodInvocationExpressionModel methodInvocationExpressionModel ->
					resolveMethodReturnType(methodInvocationExpressionModel);
			case NewClassExpressionModel newClass -> resolveNamed(newClass);
			case NamedModel named -> resolveNameUsage(named);
			case TypeModel type -> resolveType(type);
			case CastExpressionModel cast -> cast.getType().resolve(this);
			case ModifiersModel modifiersModel -> target.getParent().resolve(this);
			case LiteralExpressionModel literal -> resolveLiteral(literal);
			case ArrayDeclarationExpressionModel array -> array.getType().resolve(this);
			case ParenthesizedExpressionModel parenthesizedExpression ->
					parenthesizedExpression.getExpression().resolve(this);
			case BinaryExpressionModel binaryExpression -> resolveBinaryExpression(binaryExpression);
			case SwitchExpressionModel switchExpression -> resolveSwitchExpression(switchExpression);
			case YieldStatementModel yieldStatementModel -> yieldStatementModel.getExpression().resolve(this);
			default -> unknown();
		};
	}

	@Nonnull
	private Resolution resolveNameUsage(@Nonnull NamedModel named) {
		// First check if the named model itself is enough context to resolve.
		if (named instanceof TypeModel namedType)
			return resolveType(namedType);
		else if (named instanceof MethodReferenceExpressionModel methodReference)
			return resolveMethodInContext(methodReference.getQualifier().resolve(this),
					methodReference, methodReference.getName());

		// Next check if we can ascertain what kind of content the named model is based
		// on the surrounding context.
		Model parent = named.getParent();
		if (parent instanceof ClassModel
				|| parent instanceof ImplementsModel
				|| parent instanceof PermitsModel
				|| parent instanceof CastExpressionModel
				|| parent instanceof ThrowStatementModel
				|| parent instanceof TypeParameterModel)
			// The named model is used in a context where it can only be a dot-name.
			return resolveNamed(named);
		else if (parent instanceof InstanceofExpressionModel instanceOf
				&& instanceOf.getType() == named)
			// Only solve as a name if the name is the instanceof expression's targeted type.
			// If it's the expression portion (the thing being checked) we don't want to handle that as a dot-name.
			return resolveNamed(named);
		else if (parent instanceof MethodModel method
				&& (named.equals(method.getReturnType()) || method.getThrownTypes().contains(named)))
			// Only solve as a name if the name is used as a type name in the method model.
			return resolveNamed(named);
		else if (parent instanceof AnnotationExpressionModel anno
				&& named.equals(anno.getNameModel()))
			// Only solve as a name if the name is used as the annotation's type.
			return resolveNamed(named);
		else if (parent instanceof NewClassExpressionModel newExpr) {
			// The named model is the identifier of a 'new T()' expression so resolve as T.
			if (newExpr.getIdentifier() == named)
				return resolveNamed(newExpr);

			// The named model is an argument, so for 'new Box<T>' we would be resolving T.
			if (newExpr.getTypeArguments().contains(named))
				return resolveNamed(named);
		} else if (parent instanceof TypeModel parentType)
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
			Resolution resolution = resolveNamed(named);
			if (!resolution.isUnknown())
				return resolution;
		} else if (parent instanceof MethodInvocationExpressionModel methodInvocation
				&& named == methodInvocation.getMethodSelect())
			// The named model is the method name.
			return resolveMember(methodInvocation);
		else if (parent instanceof MethodReferenceExpressionModel methodReference) {
			if (named == methodReference.getNameModel())
				return resolveNameUsage(methodReference);
			else if (named == methodReference.getQualifier()) {
				// The qualifier can be a type (as a name expression), a variable, or an expression.
				//
				// Both the type/variable cases are stored as name expression models, so we will check
				// for it being a type name here, and as a variable in the fallthrough further below.
				//
				// If its some other kind of expression it shouldn't be a named model, and thus
				// will be solved somewhere else.
				if (named instanceof NameExpressionModel) {
					Resolution resolution = resolveNamed(named);
					if (!resolution.isUnknown())
						return resolution;
				}
			}
		}

		String name = named.getName();

		// Try looking for in-scope variables within the method.
		Model containingMethod = named.getParentOfType(MethodModel.class);
		if (containingMethod != null) {
			// Outer method variables will also be in-scope.
			MethodModel outerMethod = containingMethod.getParentOfType(MethodModel.class);
			while (outerMethod != null) {
				Optional<VariableModel> outerScopedVariable = outerMethod.getRecursiveChildrenOfType(VariableModel.class).stream()
						.filter(v -> v.getRange().end() <= named.getRange().begin() && v.getName().equals(name))
						.findFirst();
				if (outerScopedVariable.isPresent()) {
					Resolution resolution = resolveVariableModel(outerScopedVariable.get());
					if (!resolution.isUnknown())
						return resolution;
				}
				outerMethod = outerMethod.getParentOfType(MethodModel.class);
			}

			// Start from innermost scope and walk out until we go beyond the current method.
			Model scope = named;
			while (scope != null && scope != containingMethod.getParent()) {
				List<VariableModel> scopedVariables = scope.getRecursiveChildrenOfType(VariableModel.class);
				for (VariableModel variable : scopedVariables) {
					// Check for matching name, and if it is within scope (basic usage after definition check)
					if (variable.getName().equals(name) && variable.getRange().end() <= named.getRange().begin()) {
						Resolution resolution = resolveVariableModel(variable);
						if (!resolution.isUnknown())
							return resolution;
					}
				}
				scope = scope.getParent();
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
			resolution = ofClass(pool, name);
		}
		return resolution;
	}

	@Nonnull
	private Resolution resolveNamed(@Nonnull NamedModel named) {
		// Try as an imported type
		Resolution resolution = resolveNameAsQualifiedOrImported(named.getName());
		if (!resolution.isUnknown())
			return resolution;

		// Try as a class
		resolution = resolveAsInnerClass(named.getName());
		if (!resolution.isUnknown())
			return resolution;

		// Try as a type argument
		resolution = resolveAsTypeArgument(named, named.getName());
		if (!resolution.isUnknown())
			return resolution;

		// If the name is "super" treat it as a variable of the parent type.
		if (named.getName().equals("super")) {
			ClassModel declaringClass = named.getParentOfType(ClassModel.class);
			if (declaringClass != null
					&& resolveClassModel(declaringClass) instanceof ClassResolution declaringClassResolution
					&& declaringClassResolution.getClassEntry().getSuperEntry() instanceof ClassEntry superEntry) {
				return ofClass(superEntry);
			}
		}

		return unknown();
	}

	@Nonnull
	private Resolution resolveSourceFragment(@Nonnull String text, int position) {
		if (text.isEmpty())
			return unknown();

		int resolvedPosition = clampPosition(position);

		// Search backward from the provided position first so duplicate text elsewhere in the unit
		// does not win over the fragment nearest to the caller's cursor/selection.
		int start = Math.max(0, resolvedPosition - text.length() + 1);
		for (int offset = resolvedPosition; offset >= start; offset--) {
			Resolution resolution = resolveMatchingSourceFragment(text, start, offset);
			if (!resolution.isUnknown())
				return resolution;
		}

		// If the nearby search misses, fall back to a full textual scan so detached tooling can still
		// resolve fragments when the supplied position is approximate.
		String unitSource = unit.getInputSource();
		int sourceIndex = unitSource.indexOf(text);
		while (sourceIndex >= 0) {
			Resolution resolution = resolveMatchingSourceFragment(text, sourceIndex, sourceIndex + text.length() - 1);
			if (!resolution.isUnknown())
				return resolution;
			sourceIndex = unitSource.indexOf(text, sourceIndex + 1);
		}

		return unknown();
	}

	@Nonnull
	private Resolution resolveMatchingSourceFragment(@Nonnull String text, int start, int end) {
		for (int astOffset = end; astOffset >= start; astOffset--) {
			Model model = unit.getDeepestNonErroneousChildAtPosition(astOffset);
			for (Model current = model; current != null; current = current.getParent()) {
				// Compare trimmed model source so callers can pass editor fragments that omit
				// surrounding formatting while still matching the underlying AST model.
				if (!text.equals(current.getSource(unit).trim()))
					continue;
				Resolution resolution = current.resolve(this);
				if (!resolution.isUnknown())
					return resolution;
			}
		}
		return unknown();
	}

	@Nonnull
	private Resolution resolveEnclosingClass(int position) {
		Model leaf = getLeafAt(position);
		ClassModel classModel = leaf instanceof ClassModel currentClass ? currentClass : leaf.getParentOfType(ClassModel.class);
		if (classModel == null)
			return unknown();
		return resolveClassModel(classModel);
	}

	@Nonnull
	private Resolution resolveFieldFromEnclosingClasses(@Nonnull String name, int position) {
		Model leaf = getLeafAt(position);
		ClassModel currentClass = leaf instanceof ClassModel classModel ? classModel : leaf.getParentOfType(ClassModel.class);
		while (currentClass != null) {
			// First ask the normal field-in-context resolver so inherited members and generic owner
			// adaptation behave the same way they do for regular member selection.
			Resolution classResolution = resolveClassModel(currentClass);
			if (classResolution instanceof ClassResolution resolvedClass) {
				Resolution fieldResolution = resolveFieldInContext(resolvedClass, name);
				if (!fieldResolution.isUnknown())
					return fieldResolution;
			}

			// Then fall back to the source model itself so partially-backed or remapped class contexts
			// still expose declared fields even when class metadata is incomplete.
			for (VariableModel field : currentClass.getFields()) {
				if (!name.equals(field.getName()))
					continue;
				Resolution fieldResolution = field.resolve(this);
				if (!fieldResolution.isUnknown())
					return fieldResolution;
			}

			currentClass = currentClass.getParentOfType(ClassModel.class);
		}
		return unknown();
	}

	@Nonnull
	private Resolution resolveTypeParameterAt(@Nonnull String name, int position) {
		Model leaf = getLeafAt(position);
		return resolveAsTypeArgument(leaf, name);
	}

	@Nonnull
	private Model getLeafAt(int position) {
		return unit.getDeepestNonErroneousChildAtPosition(clampPosition(position));
	}

	private int clampPosition(int position) {
		int sourceLength = unit.getInputSource().length();
		if (sourceLength <= 0)
			return 0;
		return Math.max(0, Math.min(position, sourceLength - 1));
	}

	private static int adjustPositionForBase(int position, @Nonnull String fullText, @Nonnull String baseText) {
		return Math.max(0, position - (fullText.length() - baseText.length()));
	}

	@Nullable
	private static AccessFragment parseTrailingMemberSelection(@Nonnull String text) {
		int memberEnd = skipWhitespaceBackward(text, text.length() - 1);
		if (memberEnd < 0 || text.charAt(memberEnd) == ')')
			return null;

		// Only peel off the final ".member" portion. The base is resolved recursively so chains like
		// "a.b.c" do not need a dedicated parser here.
		int memberStart = scanIdentifierStart(text, memberEnd);
		if (memberStart > memberEnd)
			return null;

		int separator = skipWhitespaceBackward(text, memberStart - 1);
		if (separator < 0 || text.charAt(separator) != '.')
			return null;

		String baseText = text.substring(0, separator).trim();
		if (baseText.isEmpty())
			return null;
		return new AccessFragment(baseText, text.substring(memberStart, memberEnd + 1));
	}

	@Nullable
	private static AccessFragment parseTrailingZeroArgInvocation(@Nonnull String text) {
		if (!text.endsWith(")"))
			return null;

		// This intentionally handles only the cheap/common "foo.bar()" shape. Argument-aware parsing
		// would push too much editor-specific logic into the core resolver utility layer.
		int openParen = findMatchingOpenParen(text);
		if (openParen <= 0)
			return null;
		if (!text.substring(openParen + 1, text.length() - 1).trim().isEmpty())
			return null;

		int methodEnd = skipWhitespaceBackward(text, openParen - 1);
		if (methodEnd < 0)
			return null;

		int methodStart = scanIdentifierStart(text, methodEnd);
		if (methodStart > methodEnd)
			return null;

		int separator = skipWhitespaceBackward(text, methodStart - 1);
		if (separator < 0 || text.charAt(separator) != '.')
			return null;

		String baseText = text.substring(0, separator).trim();
		if (baseText.isEmpty())
			return null;
		return new AccessFragment(baseText, text.substring(methodStart, methodEnd + 1));
	}

	private static int scanIdentifierStart(@Nonnull String text, int end) {
		int start = end;
		while (start >= 0) {
			char c = text.charAt(start);
			if (Character.isJavaIdentifierPart(c) || c == '$') {
				start--;
				continue;
			}
			break;
		}
		return start + 1;
	}

	private static int skipWhitespaceBackward(@Nonnull String text, int index) {
		while (index >= 0 && Character.isWhitespace(text.charAt(index)))
			index--;
		return index;
	}

	private static int findMatchingOpenParen(@Nonnull String text) {
		int balance = 0;
		for (int i = text.length() - 1; i >= 0; i--) {
			char c = text.charAt(i);
			if (c == ')') {
				balance++;
			} else if (c == '(') {
				balance--;
				if (balance == 0)
					return i;
			}
		}
		return -1;
	}

	@Nonnull
	private Resolution resolveAsInnerClass(@Nonnull String name) {
		String packageName = unit.getPackage().getName();

		for (ClassModel cls : unit.getRecursiveChildrenOfType(ClassModel.class)) {
			// Only visit classes with the same inner-most name, or nested source name.
			String sourceName = getSourceClassName(cls, '.');
			if (!cls.getName().equals(name) && !sourceName.equals(name))
				continue;

			// Build name with outer classes and package.
			StringBuilder nameBuilder = new StringBuilder(getSourceClassName(cls, '$'));
			if (!packageName.isEmpty())
				nameBuilder.insert(0, packageName.replace('.', '/') + '/');

			// If it's in the pool, that should be our inner class.
			ClassEntry entry = pool.getClass(nameBuilder.toString());
			if (entry != null)
				return ofClass(entry);

			// If we haven't passed a basic name check via the builder we'll try resolving the class
			// instead. This is mainly viable when the provided code is a decompilation of an inner class
			// that is being informed via 'Resolver#setDeclaredClass'.
			if (resolve(cls) instanceof ClassResolution resolvedClass &&
					resolvedClass.getClassEntry().getName().endsWith("$" + cls.getName()))
				return resolvedClass;
		}

		return unknown();
	}

	@Nonnull
	private String getSourceClassName(@Nonnull ClassModel cls, char innerSeparator) {
		StringBuilder nameBuilder = new StringBuilder(cls.getName());
		ClassModel outerCls = cls.getParentOfType(ClassModel.class);
		while (outerCls != null) {
			nameBuilder.insert(0, outerCls.getName() + innerSeparator);
			outerCls = outerCls.getParentOfType(ClassModel.class);
		}
		return nameBuilder.toString();
	}

	@Nonnull
	private Resolution resolveNameAsQualifiedOrImported(@Nonnull String name) {
		// If it is a qualified name, just do a dot-name lookup.
		if (name.indexOf('.') > 0)
			return resolveDotName(name);

		// Otherwise look for a name in the imports that match.
		for (Map.Entry<String, ClassEntry> importEntry : importedTypes.entrySet())
			if (importEntry.getKey().endsWith('/' + name))
				return ofClass(importEntry.getValue());

		// If we're in the default package look for exact name matches since there is no package prefix.
		if (unit.getPackage().isDefaultPackage()) {
			ClassEntry entry = pool.getClass(name);
			if (entry != null)
				return ofClass(entry);
		}

		return unknown();
	}

	@Nonnull
	private Resolution resolveAsTypeArgument(@Nonnull Model origin, @Nonnull String name) {
		ClassModel cls = origin.getParentOfType(ClassModel.class);
		while (cls != null) {
			// Find a type parameter with a matching name and compute the common resolution type
			// of all of its bounds. If there are no bounds then it the bounds are just 'Object'.
			for (TypeParameterModel typeParameter : cls.getTypeParameters()) {
				if (typeParameter.getName().equals(name))
					return typeParameter.getBounds().stream()
							.map(b -> b.resolve(this))
							.reduce(Resolutions::mergeWith)
							.orElse(ofClass(jlObjectEntry));
			}
			cls = cls.getParentOfType(ClassModel.class);
		}
		return unknown();
	}

	@Nullable
	private GenericType resolveGenericModel(@Nonnull Model model) {
		// Base case, types are closer to primitives in a sense (not int/long primitives,
		// but in the sense that they are more fundamental than named models).
		if (model instanceof TypeModel typeModel)
			return resolveGenericType(typeModel);

		// If it is not a type, it may be a named model (class, field, method, etc) that we can resolve to a type.
		if (model instanceof NamedModel namedModel)
			return resolveGenericNamedType(model, namedModel.getName());
		return null;
	}

	@Nullable
	private GenericType resolveGenericType(@Nonnull TypeModel type) {
		return switch (type.getKind()) {
			case PRIMITIVE -> resolvePrimitiveGenericType(type);
			case OBJECT -> resolveGenericNamedType(type, type.getIdentifier().toString());
			case PARAMETERIZED -> resolveParameterizedGenericType(type);
			case UNION -> type instanceof TypeModel.Union union ? resolveUnionGenericType(union) : null;
			case ARRAY -> type instanceof TypeModel.Array arrayType ? resolveArrayGenericType(arrayType) : null;
			case ANNOTATED -> type instanceof TypeModel.Annotated annotated ? resolveGenericType(annotated.getType()) : null;
			case WILDCARD -> type instanceof TypeModel.Wildcard wildcard ? resolveWildcardGenericType(wildcard) : null;
			case VAR -> null;
		};
	}

	@Nullable
	private GenericType resolveGenericNamedType(@Nonnull Model origin, @Nonnull String name) {
		GenericType sourceTypeVariable = resolveSourceTypeVariable(origin, name);
		if (sourceTypeVariable != null)
			return sourceTypeVariable;

		Resolution resolution = resolveNameAsQualifiedOrImported(name);
		if (resolution.isUnknown())
			resolution = resolveAsInnerClass(name);
		if (resolution instanceof ClassResolution classResolution)
			return Resolutions.getResolvedClassType(classResolution);
		return null;
	}

	@Nullable
	private GenericType resolveSourceTypeVariable(@Nonnull Model origin, @Nonnull String name) {
		ClassModel cls = origin.getParentOfType(ClassModel.class);
		while (cls != null) {
			for (TypeParameterModel typeParameter : cls.getTypeParameters())
				if (typeParameter.getName().equals(name))
					return new GenericType.TypeVariableType(new GenericTypeParameter(
							getSourceTypeOwnerId(cls), name, resolveTypeParameterUpperBound(typeParameter)));
			cls = cls.getParentOfType(ClassModel.class);
		}
		return null;
	}

	@Nonnull
	private String getSourceTypeOwnerId(@Nonnull ClassModel cls) {
		if (resolveClassModel(cls) instanceof ClassResolution classResolution)
			return classResolution.getClassEntry().getName();

		StringBuilder builder = new StringBuilder(cls.getName());
		ClassModel outerClass = cls.getParentOfType(ClassModel.class);
		while (outerClass != null) {
			builder.insert(0, outerClass.getName() + '$');
			outerClass = outerClass.getParentOfType(ClassModel.class);
		}
		if (!unit.getPackage().isDefaultPackage())
			builder.insert(0, unit.getPackage().getName().replace('.', '/') + '/');
		return builder.toString();
	}

	@Nonnull
	private DescribableEntry resolveTypeParameterUpperBound(@Nonnull TypeParameterModel typeParameter) {
		return typeParameter.getBounds().stream()
				.map(bound -> bound.resolve(this))
				.filter(DescribableResolution.class::isInstance)
				.map(DescribableResolution.class::cast)
				.map(DescribableResolution::getDescribableEntry)
				.reduce(this::getCommonDescriptor)
				.orElse(jlObjectEntry);
	}

	@Nullable
	private GenericType resolvePrimitiveGenericType(@Nonnull TypeModel type) {
		if (!(type instanceof TypeModel.Primitive primitiveType))
			return null;
		return switch (primitiveType.getPrimitiveKind()) {
			case BOOLEAN -> GenericTypes.ofPrimitive(getPrimitive("Z"));
			case BYTE -> GenericTypes.ofPrimitive(getPrimitive("B"));
			case SHORT -> GenericTypes.ofPrimitive(getPrimitive("S"));
			case INT -> GenericTypes.ofPrimitive(getPrimitive("I"));
			case LONG -> GenericTypes.ofPrimitive(getPrimitive("J"));
			case CHAR -> GenericTypes.ofPrimitive(getPrimitive("C"));
			case FLOAT -> GenericTypes.ofPrimitive(getPrimitive("F"));
			case DOUBLE -> GenericTypes.ofPrimitive(getPrimitive("D"));
			case VOID -> GenericTypes.ofPrimitive(getPrimitive("V"));
			default -> null;
		};
	}

	@Nullable
	private GenericType resolveParameterizedGenericType(@Nonnull TypeModel type) {
		if (!(type instanceof TypeModel.Parameterized parameterizedType))
			return null;
		GenericType identifierType = resolveGenericModel(parameterizedType.getIdentifier());
		if (!(GenericTypes.toUsableType(identifierType, jlObjectEntry) instanceof GenericType.ClassType classType))
			return null;

		List<GenericType> typeArguments = new ArrayList<>(parameterizedType.getTypeArguments().size());
		for (Model typeArgument : parameterizedType.getTypeArguments()) {
			GenericType resolvedArgument = resolveGenericModel(typeArgument);
			if (resolvedArgument == null)
				return null;
			typeArguments.add(resolvedArgument);
		}
		return new GenericType.ClassType(classType.classEntry(), typeArguments);
	}

	@Nullable
	private GenericType resolveUnionGenericType(@Nonnull TypeModel.Union union) {
		ClassEntry common = null;
		for (TypeModel unionArgType : union.getAllTypes()) {
			GenericType resolvedArg = resolveGenericType(unionArgType);
			GenericType usableArg = GenericTypes.toUsableType(resolvedArg, jlObjectEntry);
			if (usableArg instanceof GenericType.ClassType classType) {
				ClassEntry resolvedClass = classType.classEntry();
				common = common == null ? resolvedClass : common.getCommonParent(resolvedClass);
			}
		}
		return common == null ? null : GenericTypes.ofClass(common);
	}

	@Nullable
	private GenericType resolveArrayGenericType(@Nonnull TypeModel.Array arrayType) {
		GenericType elementType = resolveGenericType(arrayType.getRootModel());
		if (elementType == null)
			return null;
		return new GenericType.ArrayType(elementType, arrayType.getDimensions());
	}

	@Nonnull
	private GenericType resolveWildcardGenericType(@Nonnull TypeModel.Wildcard wildcard) {
		Model boundModel = wildcard.getBound();
		if (boundModel == null)
			return new GenericType.WildcardType(null, null, jlObjectEntry);

		GenericType boundType = resolveGenericModel(boundModel);
		if (boundType == null)
			return new GenericType.WildcardType(null, null, jlObjectEntry);

		boolean isLowerBound = wildcard.getIdentifier().toString().contains(" super ");
		if (isLowerBound)
			return new GenericType.WildcardType(null, boundType, jlObjectEntry);

		GenericType usableBound = GenericTypes.toUsableType(boundType, jlObjectEntry);
		return new GenericType.WildcardType(usableBound, null, usableBound.asDescribable());
	}

	@Nonnull
	private Resolution toGenericResolution(@Nullable GenericType genericType) {
		// Collapse the internal generic model back into the public resolution types,
		// preserving parameterized class info when the caller can still use it.
		genericType = canonicalizeGenericType(genericType);
		GenericType usableType = GenericTypes.toUsableType(genericType, jlObjectEntry);
		if (usableType == null)
			return unknown();

		DescribableEntry describableEntry = usableType.asDescribable();
		if (usableType instanceof GenericType.ClassType classType)
			return ofClass(classType);
		return switch (describableEntry) {
			case ArrayEntry arrayEntry -> ofArray(arrayEntry);
			case PrimitiveEntry primitiveEntry -> ofPrimitive(primitiveEntry);
			case NullEntry nullEntry -> nul();
			case ClassEntry classEntry -> ofClass(classEntry);
			default -> unknown();
		};
	}

	@Nullable
	private GenericType getResolvedGenericType(@Nonnull Resolution resolution) {
		// Lift a resolution back into the richer generic model so chained lookups
		// can preserve concrete type arguments instead of falling back to erasure.
		return switch (resolution) {
			case FieldResolution fieldResolution -> Resolutions.getResolvedFieldGenericType(fieldResolution);
			case MethodResolution methodResolution -> Resolutions.getResolvedMethodReturnGenericType(methodResolution);
			case VariableResolution variableResolution -> Resolutions.getResolvedVariableGenericType(variableResolution);
			case ClassResolution classResolution -> Resolutions.getResolvedClassType(classResolution);
			case ArrayResolution arrayResolution -> {
				GenericType elementType = getResolvedGenericType(arrayResolution.getElementTypeResolution());
				yield elementType == null ? null : new GenericType.ArrayType(elementType, arrayResolution.getDimensions());
			}
			case PrimitiveResolution primitiveResolution ->
					new GenericType.PrimitiveType(primitiveResolution.getPrimitiveEntry());
			case NullResolution nullResolution -> null;
			case DescribableResolution describableResolution ->
					rawGenericType(describableResolution.getDescribableEntry());
			default -> null;
		};
	}

	@Nullable
	private GenericType rawGenericType(@Nullable DescribableEntry entry) {
		if (entry == null)
			return null;

		// Rebuild a generic type shell from erased entries when no better generic
		// information exists, so later code can still use one common type pipeline.
		return switch (entry) {
			case ClassEntry classEntry -> GenericTypes.ofClass(classEntry);
			case PrimitiveEntry primitiveEntry -> GenericTypes.ofPrimitive(primitiveEntry);
			case ArrayEntry arrayEntry -> {
				GenericType elementType = rawGenericType(arrayEntry.getElementEntry());
				yield elementType == null ? null : new GenericType.ArrayType(elementType, arrayEntry.getDimensions());
			}
			case NullEntry ignored -> null;
			case MemberEntry ignored -> null;
		};
	}

	@Nullable
	private GenericType canonicalizeGenericType(@Nullable GenericType genericType) {
		if (genericType == null)
			return null;
		return switch (genericType) {
			case GenericType.PrimitiveType primitiveType -> primitiveType;
			case GenericType.TypeVariableType typeVariableType -> typeVariableType;
			case GenericType.ArrayType arrayType -> {
				GenericType elementType = canonicalizeGenericType(arrayType.elementType());
				yield elementType == arrayType.elementType() ? arrayType : new GenericType.ArrayType(elementType, arrayType.dimensions());
			}
			case GenericType.WildcardType wildcardType -> {
				GenericType upperBound = canonicalizeGenericType(wildcardType.upperBound());
				GenericType lowerBound = canonicalizeGenericType(wildcardType.lowerBound());
				yield upperBound == wildcardType.upperBound() && lowerBound == wildcardType.lowerBound() ? wildcardType :
						new GenericType.WildcardType(upperBound, lowerBound, wildcardType.erasure());
			}
			case GenericType.ClassType classType -> canonicalizeClassType(classType);
		};
	}

	@Nonnull
	private GenericType.ClassType canonicalizeClassType(@Nonnull GenericType.ClassType classType) {
		ClassEntry classEntry = classType.classEntry();
		ClassEntry canonicalEntry = Objects.requireNonNullElse(pool.getClass(classEntry.getName()), classEntry);
		List<GenericType> typeArguments = classType.typeArguments();
		List<GenericType> canonicalTypeArguments = typeArguments;
		for (int i = 0; i < typeArguments.size(); i++) {
			GenericType originalArgument = typeArguments.get(i);
			GenericType canonicalArgument = canonicalizeGenericType(originalArgument);
			if (canonicalArgument != originalArgument) {
				if (canonicalTypeArguments == typeArguments)
					canonicalTypeArguments = new ArrayList<>(typeArguments);
				canonicalTypeArguments.set(i, canonicalArgument);
			}
		}
		if (canonicalEntry == classEntry && canonicalTypeArguments == typeArguments)
			return classType;
		return new GenericType.ClassType(canonicalEntry, canonicalTypeArguments);
	}

	@Nullable
	private GenericType.ClassType getDirectSuperType(@Nonnull GenericType.ClassType ownerType) {
		// Rebind the declared generic superclass through the current receiver so
		// parent lookups see Example<String> -> Parent<String> instead of raw Parent.
		ownerType = canonicalizeClassType(ownerType);
		GenericType.ClassType genericSuperType = ownerType.classEntry().getGenericSuperType();
		if (genericSuperType == null)
			return null;
		return canonicalizeClassType(Objects.requireNonNull(GenericTypes.asClassType(
				GenericTypes.substitute(genericSuperType, GenericTypes.bind(ownerType)), jlObjectEntry)));
	}

	@Nonnull
	private List<GenericType.ClassType> getDirectInterfaceTypes(@Nonnull GenericType.ClassType ownerType) {
		// Apply the receiver's bindings to each directly implemented interface so
		// inherited members resolve against the concrete interface arguments.
		ownerType = canonicalizeClassType(ownerType);
		List<GenericType.ClassType> interfaceTypes = new ArrayList<>();
		Map<GenericTypeParameter, GenericType> bindings = GenericTypes.bind(ownerType);
		for (GenericType.ClassType interfaceType : ownerType.classEntry().getGenericInterfaceTypes()) {
			GenericType.ClassType substitutedType = GenericTypes.asClassType(GenericTypes.substitute(interfaceType, bindings), jlObjectEntry);
			if (substitutedType != null)
				interfaceTypes.add(canonicalizeClassType(substitutedType));
		}
		return interfaceTypes;
	}

	@Nonnull
	private FieldResolution adaptFieldResolution(@Nonnull GenericType.ClassType ownerType, @Nonnull FieldEntry fieldEntry) {
		// Rewrite the declared field type through the receiver bindings so T-backed
		// fields like Box<T>.value surface as Box<String>.value -> String.
		ownerType = canonicalizeClassType(ownerType);
		GenericType resolvedFieldType = GenericTypes.substitute(fieldEntry.getGenericType(), GenericTypes.bind(ownerType));
		return Resolutions.ofField(ownerType, fieldEntry, resolvedFieldType);
	}

	@Nonnull
	private MethodResolution adaptMethodResolution(@Nonnull GenericType.ClassType ownerType, @Nonnull MethodEntry methodEntry) {
		// Receiver-only adaptation is enough for non-generic methods declared on a
		// parameterized owner such as List<String>.get(int) -> String.
		ownerType = canonicalizeClassType(ownerType);
		return adaptMethodResolution(ownerType, methodEntry, GenericTypes.bind(ownerType));
	}

	@Nonnull
	private MethodResolution adaptMethodResolution(@Nonnull GenericType.ClassType ownerType, @Nonnull MethodEntry methodEntry,
	                                               @Nonnull Map<GenericTypeParameter, GenericType> bindings) {
		// Apply the final binding map to both return and parameter types so downstream
		// lookups can continue from the adapted signature instead of the erased one.
		ownerType = canonicalizeClassType(ownerType);
		GenericType resolvedReturnType = GenericTypes.substitute(methodEntry.getGenericReturnType(), bindings);
		List<GenericType> resolvedParameterTypes = methodEntry.getGenericParameterTypes().stream()
				.map(type -> GenericTypes.substitute(type, bindings))
				.toList();
		return Resolutions.ofMethod(ownerType, methodEntry, resolvedReturnType, resolvedParameterTypes);
	}

	@Nullable
	private MethodResolution resolveFunctionalInterfaceMethod(@Nonnull GenericType.ClassType lambdaType) {
		// Reduce a functional interface to its single abstract method, keeping any
		// receiver-applied type arguments so lambda parameters/returns stay concrete.
		List<MethodEntry> abstractMethods = lambdaType.classEntry().getDeclaredMethods().stream()
				.filter(MethodEntry::isAbstract)
				.toList();
		if (abstractMethods.size() != 1)
			return null;
		return adaptMethodResolution(lambdaType, abstractMethods.getFirst());
	}

	private void visitBoundHierarchy(@Nonnull GenericType.ClassType ownerType, @Nonnull Consumer<GenericType.ClassType> consumer) {
		// Walk the hierarchy using already-substituted parent/interface types so every
		// visited owner preserves the same concrete generic view as the original receiver.
		consumer.accept(ownerType);
		GenericType.ClassType directSuperType = getDirectSuperType(ownerType);
		if (directSuperType != null)
			visitBoundHierarchy(directSuperType, consumer);
		for (GenericType.ClassType interfaceType : getDirectInterfaceTypes(ownerType))
			visitBoundHierarchy(interfaceType, consumer);
	}

	@Nonnull
	private Resolution resolveType(@Nonnull TypeModel type) {
		// Type-model resolution now flows through the generic type model first so
		// parameterized forms like List<String> survive initial resolution.
		return toGenericResolution(resolveGenericType(type));
	}

	@Nonnull
	private Resolution resolveAsIdentifier(@Nonnull Model identifier) {
		// Identifiers in type positions should keep generic structure, while other
		// named references still use the normal name-resolution path.
		if (identifier instanceof TypeModel typeIdentifier)
			return resolveType(typeIdentifier);
		else if (identifier instanceof NamedModel named)
			return resolveNamed(named);
		return unknown();
	}

	@Nonnull
	private Resolution resolvePackageModel(@Nonnull PackageModel pkg) {
		String packageName = pkg.isDefaultPackage() ? null : pkg.getName().replace('.', '/');
		return ofPackage(packageName);
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
				List<ClassMemberPair> memberEntries = new ArrayList<>();
				if (memberName.lastIndexOf('*') >= 0) {
					// Star import, so all members of the class should be returned.
					declaringClassResolution.getClassEntry().visitHierarchy(owner -> memberEntries.addAll(owner.declaredMemberStream()
							.filter(e -> !e.isPrivate() && e.isStatic())
							.map(e -> new ClassMemberPair(owner, e))
							.toList()));

				} else {
					// Specific name import, so only members with the same name should be returned.
					declaringClassResolution.getClassEntry().visitHierarchy(owner -> memberEntries.addAll(owner.declaredMemberStream()
							.filter(e -> !e.isPrivate() && e.isStatic() && e.getName().equals(memberName))
							.map(e -> new ClassMemberPair(owner, e))
							.toList()));
				}
				return ofMembers(memberEntries);
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

		return resolveNameAsQualifiedOrImported(name);
	}

	@Nonnull
	private Resolution resolveClassModel(@Nonnull ClassModel clazz) {
		// First check if there are any externally provided class entries for this class model.
		if (externallyResolvedClassEntries != null) {
			// If there is, we will trust the externally provided resolution.
			ClassEntry declaredClassEntry = externallyResolvedClassEntries.get(clazz);
			if (declaredClassEntry != null)
				return ofClass(declaredClassEntry);
		}

		// Otherwise we'll try to do a name lookup and resolve locally against known classes in the pool.
		String name = clazz.getName();
		ClassModel outerClass = clazz.getParentOfType(ClassModel.class);
		if (outerClass != null && resolveClassModel(outerClass) instanceof ClassResolution outerResolution)
			return resolveDotName(outerResolution.getClassEntry().getName() + '.' + name);
		if (unit.getPackage().isDefaultPackage())
			return ofClass(pool, name);
		return resolveDotName(unit.getPackage().getName() + '.' + name);
	}

	@Nonnull
	private Resolution resolveAnnotationArgument(@Nonnull AnnotationArgumentModel argument) {
		// We want to resolve the named argument.
		// In some cases the argument will have no name for the implicit 'value' case.
		// Once we resolve the declaring annotation type, we can look in it for matching methods.
		Resolution containingAnnotationResolution = Objects.requireNonNull(argument.getParent()).resolve(this);
		if (!containingAnnotationResolution.isUnknown())
			return resolveMethodInContext(containingAnnotationResolution, argument, argument.getName());
		return unknown();
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
		MethodResolution byNameResolution = null;
		if (methodName.charAt(0) == '<') {
			if (resolveMethodByNameInClass(definingClassEntry, methodName, getPrimitive("V"), null) instanceof MethodResolution resolution)
				byNameResolution = resolution;
		} else if (resolveMethodByNameInClass(definingClassEntry, methodName) instanceof MethodResolution resolution)
			byNameResolution = resolution;

		// If the resolution comes from this defining class, then the by-name lookup should be fine.
		if (byNameResolution != null && byNameResolution.getOwnerEntry() == definingClassEntry)
			return byNameResolution;

		// It seems that either the by-name lookup did not narrow us down to a single resolution that we could use
		// in the current defining class... We will need to resolve the descriptor then look up with that.
		//
		// We are not going to use the resolution as a fallback value beyond this point because then it would
		// be a method defined in a parent class, which would be wrong since this AST model is a declaration
		// in the current class.
		if (!(method.getReturnType().resolve(this) instanceof DescribableResolution resolvedReturnType))
			return unknown();
		List<VariableModel> parameters = method.getParameters();
		List<DescribableEntry> describableParameters = new ArrayList<>(parameters.size());
		for (VariableModel parameter : parameters) {
			// The resolved type may be null if the parameter type is something we don't have access to.
			// However, we want to keep these nulls in the list so that the parameter count is correct for the method descriptor.
			DescribableEntry resolvedParameterType = Resolutions.getResolvedValueType(parameter.resolve(this));
			describableParameters.add(resolvedParameterType);
		}

		// Resolve by name/descriptor.
		//  - If all parameter types are resolved, we can use the method descriptor to resolve the method.
		//  - If any parameter type is unresolved, we will need to resolve by name and parameter count, and then filter by the resolved return type.
		Resolution resolution;
		if (describableParameters.stream().allMatch(Objects::nonNull))
			resolution = ofMethod(definingClassEntry, methodName, resolvedReturnType.getDescribableEntry(), describableParameters);
		else
			resolution = resolveMethodByNameInClass(GenericTypes.ofClass(definingClassEntry), methodName,
					rawGenericType(resolvedReturnType.getDescribableEntry()), toGenericTypeHints(describableParameters), describableParameters);

		// For constructors of inner classes, try again with the synthetic outer class parameter added.
		if (methodName.charAt(0) == '<' && (resolution.isUnknown() || resolution instanceof MethodResolution mr && !mr.getOwnerEntry().getName().equals(definingClassEntry.getName()))) {
			// Check if we have an outer class. If not, there is nothing more we can do.
			ClassEntry outerClass = definingClassEntry.getOuterClass();
			if (outerClass != null) {
				// Same resolve by name/descriptor, but with the added synthetic parameter.
				describableParameters.addFirst(outerClass);

				// If we do not find a result we want to retain our existing resolution.
				// Same idea applies here as above, if all parameter types are resolved, we can use the method descriptor to resolve the method.
				Resolution synthCtorResolution;
				if (describableParameters.stream().allMatch(Objects::nonNull))
					synthCtorResolution = ofMethod(definingClassEntry, methodName, resolvedReturnType.getDescribableEntry(), describableParameters);
				else
					synthCtorResolution = resolveMethodByNameInClass(GenericTypes.ofClass(definingClassEntry), methodName,
							rawGenericType(resolvedReturnType.getDescribableEntry()), toGenericTypeHints(describableParameters), describableParameters);
				if (!synthCtorResolution.isUnknown())
					resolution = synthCtorResolution;
			}
		}

		return resolution;
	}

	@Nonnull
	private Resolution resolveVariableModel(@Nonnull VariableModel variable) {
		Resolution typeResolution = resolveVariableType(variable);
		GenericType genericType = getResolvedGenericType(typeResolution);
		if (genericType != null)
			return ofVariable(variable.getName(), genericType);

		DescribableEntry resolvedType = Resolutions.getResolvedValueType(typeResolution);
		return resolvedType == null ? unknown() : ofVariable(variable.getName(), resolvedType);
	}

	@Nonnull
	private Resolution resolveFieldByNameInClass(@Nonnull ClassEntry declaringClass, @Nonnull String fieldName,
	                                             @Nullable DescribableEntry typeEntryHint) {
		return resolveFieldByNameInClass(GenericTypes.ofClass(declaringClass), fieldName, typeEntryHint);
	}

	@Nonnull
	private Resolution resolveFieldByNameInClass(@Nonnull GenericType.ClassType declaringType, @Nonnull String fieldName,
	                                             @Nullable DescribableEntry typeEntryHint) {
		ClassEntry declaringClass = declaringType.classEntry();

		// Edge case for implicit "this" class variable.
		if (fieldName.equals("this"))
			return ofClass(declaringType);

		// Edge case for cases like "String.class"
		if (fieldName.equals("class"))
			return ofClass(jlClassEntry);

		// Check if the field is declared in this class, and is unique in the hierarchy in terms of signature.
		List<FieldEntry> fieldsByName = declaringClass.getDeclaredFieldsByName(fieldName);
		if (fieldsByName.size() == 1)
			return adaptFieldResolution(declaringType, fieldsByName.getFirst());

		// Check if the fields can be differentiated by the given type hint.
		if (typeEntryHint != null)
			for (FieldEntry fieldEntry : fieldsByName)
				if (adaptFieldResolution(declaringType, fieldEntry).getResolvedFieldType().isAssignableFrom(typeEntryHint))
					return adaptFieldResolution(declaringType, fieldEntry);

		// Check in super-type.
		GenericType.ClassType directSuperType = getDirectSuperType(declaringType);
		if (directSuperType != null
				&& resolveFieldByNameInClass(directSuperType, fieldName, typeEntryHint) instanceof FieldResolution resolution)
			return resolution;

		// Check in interfaces.
		for (GenericType.ClassType implementedType : getDirectInterfaceTypes(declaringType))
			if (resolveFieldByNameInClass(implementedType, fieldName, typeEntryHint) instanceof FieldResolution resolution)
				return resolution;

		return unknown();
	}

	@Nonnull
	private Resolution resolveMethodByNameInClass(@Nonnull ClassEntry classEntry, @Nonnull String methodName) {
		return resolveMethodByNameInClass(GenericTypes.ofClass(classEntry), methodName, null, null, null);
	}

	@Nonnull
	private Resolution resolveMethodByNameInClass(@Nonnull ClassEntry classEntry, @Nonnull String methodName,
	                                              @Nullable DescribableEntry returnTypeEntry,
	                                              @Nullable List<DescribableEntry> argumentTypeEntries) {
		return resolveMethodByNameInClass(GenericTypes.ofClass(classEntry), methodName,
				rawGenericType(returnTypeEntry), toGenericTypeHints(argumentTypeEntries), argumentTypeEntries);
	}

	@Nonnull
	private Resolution resolveMethodByNameInClass(@Nonnull GenericType.ClassType classType, @Nonnull String methodName,
	                                              @Nullable GenericType returnTypeHint,
	                                              @Nullable List<GenericType> argumentTypeHints,
	                                              @Nullable List<DescribableEntry> argumentTypeEntries) {
		ClassEntry classEntry = classType.classEntry();
		DescribableEntry returnTypeEntry = null;
		if (returnTypeHint != null) {
			GenericType usableReturnType = GenericTypes.toUsableType(returnTypeHint, jlObjectEntry);
			if (usableReturnType != null)
				returnTypeEntry = usableReturnType.asDescribable();
		}

		// Check if the method is declared in this class.
		//  - Only one match by name   --> match
		//  - Multiple matches by name --> filter by matching signature --> match
		List<MethodEntry> methodsByName = classEntry.getDeclaredMethodsByName(methodName);
		if (methodsByName.size() == 1)
			return adaptMethodResolution(classType, methodsByName.getFirst(), returnTypeHint, argumentTypeHints);
		if (methodsByName.size() > 1 && (returnTypeEntry != null || argumentTypeEntries != null)) {
			// Try and prune candidates by filtering against presumed return/argument types.
			for (int i = methodsByName.size() - 1; i >= 0; i--) {
				MethodEntry methodEntry = methodsByName.get(i);
				MethodResolution adaptedMethod = adaptMethodResolution(classType, methodEntry, returnTypeHint, argumentTypeHints);

				// Prune method candidates with mismatching return types.
				if (returnTypeEntry != null) {
					DescribableEntry describableReturn = adaptedMethod.getResolvedReturnType();
					if (!describableReturn.isAssignableFrom(returnTypeEntry)) {
						methodsByName.remove(methodEntry);
						continue;
					}
				}

				// Prune method candidates with mismatching argument types.
				if (argumentTypeEntries != null) {
					List<GenericType> parameterTypes = Resolutions.getResolvedMethodParameterGenericTypes(adaptedMethod);
					int hintedArgCount = argumentTypeEntries.size();
					int actualArgCount = parameterTypes.size();
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
						GenericType usableVarargType = GenericTypes.toUsableType(parameterTypes.getLast(), jlObjectEntry);
						if (usableVarargType instanceof GenericType.ArrayType arrayType) {
							DescribableEntry varargElementType = arrayType.elementType().asDescribable();
							boolean methodRemoved = false;
							for (int j = maxArgToCheck; j < hintedArgCount; j++) {
								DescribableEntry argumentType = argumentTypeEntries.get(j);
								if (argumentType != null && !varargElementType.isAssignableFrom(argumentType)) {
									methodRemoved = true;
									methodsByName.remove(methodEntry);
									break;
								}
							}
							if (methodRemoved)
								continue;
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
						DescribableEntry describableParameter = parameterTypes.get(j).asDescribable();
						DescribableEntry argType = argumentTypeEntries.get(j);
						if (argType == null) // Can be null for unresolved types, we cannot prune in that case.
							continue;

						// Multi-class entries are special and created from our inference logic.
						// In essence, the arg can be "one of multiple" options.
						// Example:
						//   void consume(Supplier)
						//   void consume(IntSupplier)
						// Multiple types:
						//   Supplier
						//   IntSupplier
						// The types do not have a common type other than 'Object' which isn't helpful.
						// We will see our argument is ideally one of these options then prune the other.
						if (argType instanceof MultiClassEntry multi) {
							if (multi.getClassEntries().stream().noneMatch(describableParameter::isAssignableFrom)) {
								methodsByName.remove(methodEntry);
								break;
							}
						}
						// Otherwise, prune if the parameter is not assignable from the argument type.
						else if (!describableParameter.isAssignableFrom(argType)) {
							methodsByName.remove(methodEntry);
							break;
						}
					}
				}
			}

			// Check again after pruning if there is only a single candidate.
			if (methodsByName.size() == 1)
				return adaptMethodResolution(classType, methodsByName.getFirst(), returnTypeHint, argumentTypeHints);

			// Check and see if there is an exact descriptor match.
			//  TODO: Case where the returnValue but not args are given, case where both are given
			if (argumentTypeEntries != null && argumentTypeEntries.stream().allMatch(Objects::nonNull)) {
				String argsDesc = "(" + argumentTypeEntries.stream().map(DescribableEntry::getDescriptor).collect(Collectors.joining("")) + ")";
				methodsByName = methodsByName.stream()
						.filter(e -> e.getDescriptor().startsWith(argsDesc))
						.collect(Collectors.toList());
				if (methodsByName.size() == 1)
					return adaptMethodResolution(classType, methodsByName.getFirst(), returnTypeHint, argumentTypeHints);
			}
		}

		// In some cases we want to check for the method in parent classes:
		//  - Super-class
		//  - Interfaces
		// Just not when the method name is special, like a constructor or the static initializer.
		if (methodName.charAt(0) != '<') {
			GenericType.ClassType directSuperType = getDirectSuperType(classType);
			if (directSuperType != null
					&& resolveMethodByNameInClass(directSuperType, methodName, returnTypeHint, argumentTypeHints, argumentTypeEntries) instanceof MethodResolution resolution)
				return resolution;
			for (GenericType.ClassType implementedType : getDirectInterfaceTypes(classType))
				if (resolveMethodByNameInClass(implementedType, methodName, returnTypeHint, argumentTypeHints, argumentTypeEntries) instanceof MethodResolution resolution)
					return resolution;
		}

		return unknown();
	}

	@Nonnull
	private Resolution resolveMemberByNameInModel(@Nonnull Model origin, @Nonnull String name, @Nonnull MemberTarget target) {
		// If the name is used within an annotation use-case, we only want to look in the declared annotation.
		if (origin.getParent() instanceof AnnotationArgumentModel annotationArgument
				&& annotationArgument.getName().equals(name))
			return annotationArgument.resolve(this);

		// Look for members of the requested target type in the surrounding class context.
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
						resolveMethodByNameInClass(GenericTypes.ofClass(classEntry), name, null,
								collectGenericMethodArgumentsInParentContext(origin),
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
							return resolveMemberInContext(methodResolution.getOwnerResolution(), invocation, name);
						} else if (importResolution instanceof MultiMemberResolution multiMemberresolution) {
							for (ClassMemberPair pair : multiMemberresolution.getMemberEntries()) {
								MemberEntry memberEntry = pair.memberEntry();
								if (memberEntry.isMethod() && memberEntry.getName().equals(name)) {
									Resolution entryResolution = resolveMemberInContext(ofClass(pair.ownerEntry()), invocation, name);
									if (!entryResolution.isUnknown())
										return entryResolution;
								}
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
		// If the class doesn't declare a static initializer, we will return a stub resolution for it.
		List<MethodEntry> initializers = resolvedDefiningClass.getClassEntry().getDeclaredMethodsByName("<clinit>");
		MethodEntry initializer = initializers.isEmpty() ?
				STATIC_INIT_STUB :
				initializers.getFirst();
		return ofMethod(resolvedDefiningClass.getClassEntry(), initializer);
	}

	@Nonnull
	private Resolution resolveVariableType(@Nonnull VariableModel variable) {
		TypeModel type = variable.getType();

		// If the type is 'var' then solve based on the initializer or usage.
		if (type.getKind() == TypeModel.Kind.VAR) {
			Model value = variable.getValue();

			// First try to resolve based on the initializer if it is present
			// since that is more likely to be correct than usage-based inference.
			if (value != null) {
				Resolution valueResolution = toValueTypeResolution(value.resolve(this));
				if (!valueResolution.isUnknown())
					return valueResolution;
			}

			// No initializer so we have to try inferring from usage.
			DescribableEntry inferredType = inferFromUsage(variable, false);
			if (inferredType != null)
				return ofDescribable(inferredType);

			return unknown();
		}

		// Otherwise resolve declared type
		return resolveType(type);
	}

	@Nonnull
	private Resolution toValueTypeResolution(@Nonnull Resolution resolution) {
		GenericType genericType = getResolvedGenericType(resolution);
		if (genericType != null)
			return toGenericResolution(genericType);
		return Resolutions.toValueTypeResolution(resolution);
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
		if (memberName.equals("super") && contextResolution instanceof ClassResolution classContextResolution) {
			ClassEntry superEntry = classContextResolution.getClassEntry().getSuperEntry();
			if (superEntry != null)
				return ofClass(superEntry);
		}
		return resolveMemberInContext(contextResolution, memberSelect, memberName);
	}

	@Nonnull
	private Resolution resolveMemberInContext(@Nonnull Resolution contextResolution, @Nonnull Model origin, @Nonnull String memberName) {
		return switch (contextResolution) {
			case ClassResolution classResolution -> {
				// Member name can be a field or method identifier in the context of the resolved class.
				//  - static imports
				//      - MY_CONST
				//      - parseInt(...)
				//  - class context
				//      - Constants.MY_CONST
				//      - Constants.getMyConst()
				if (origin instanceof MethodInvocationExpressionModel) {
					// We're looking for a method then.
					yield resolveMethodInContext(contextResolution, origin, memberName);
				} else if (origin instanceof MemberSelectExpressionModel) {
					// Member selections can be a field or method.
					// Check what the parent is to determine if we're looking for a field or method.
					if (origin.getParent() instanceof MethodInvocationExpressionModel invoke && invoke.getMethodSelect() == origin)
						yield resolveMethodInContext(contextResolution, origin, memberName);
					yield resolveFieldInContext(contextResolution, origin, memberName);
				} else {
					// We're not sure what we're looking for, so try both.
					Resolution resolution = resolveFieldInContext(contextResolution, origin, memberName);
					yield resolution.isUnknown() ?
							resolveMethodInContext(contextResolution, origin, memberName) : resolution;
				}
			}
			case FieldResolution fieldResolution -> {
				// The identifier is in the context of another member identifier such as:
				//  - someField.targetName
				//  - someField.targetName()
				GenericType fieldType = getResolvedGenericType(fieldResolution);
				yield fieldType != null ? resolveMemberInContext(toGenericResolution(fieldType), origin, memberName) : unknown();
			}
			case VariableResolution variableResolution -> {
				// The identifier is in the context of a local variable or method parameter such as:
				//  - someLocal.targetName
				//  - someLocal.targetName()
				GenericType variableType = getResolvedGenericType(variableResolution);
				yield variableType != null ? resolveMemberInContext(toGenericResolution(variableType), origin, memberName) : unknown();
			}
			case MethodResolution methodResolution -> {
				// The identifier is in the context of another member identifier such as:
				//  - someMethod().targetName
				//  - someMethod().targetName()
				GenericType returnType = getResolvedGenericType(methodResolution);
				yield returnType != null ? resolveMemberInContext(toGenericResolution(returnType), origin, memberName) : unknown();
			}
			case ArrayResolution arrayResolution -> {
				// The identifier is in the context of another member identifier representing an array variable such as:
				//  - args.length
				//  - args.clone()
				Resolution resolution = resolveFieldInContext(contextResolution, origin, memberName);
				yield !resolution.isUnknown() ? resolution : resolveMemberInContext(ofClass(jlObjectEntry), origin, memberName);
			}
			default -> unknown();
		};
	}

	@Nullable
	private DescribableEntry inferFromUsage(@Nonnull Model origin, boolean adaptLambdaUsage) {
		if (!usageInferenceInProgress.add(origin))
			return null;

		try {
			DescribableEntry usageType = null;
			Model parent = origin.getParent();
			if (parent instanceof VariableModel variable && variable.getValue() == origin) {
				// String known = foo --> We know 'foo' must be 'String'
				Resolution targetRes = variable.getType().resolve(this);
				if (targetRes instanceof DescribableResolution desc)
					usageType = desc.getDescribableEntry();
			} else if (parent instanceof AssignmentExpressionModel assign && assign.getVariable() == origin) {
				// Unknown.foo = "string" --> We know 'foo' must be 'String'
				Resolution targetRes = assign.getExpression().resolve(this);
				if (targetRes instanceof DescribableResolution desc)
					usageType = desc.getDescribableEntry();
			} else if (parent instanceof MethodInvocationExpressionModel invoke) {
				// String.copyValueOf(Unknown.foo, 0, 0)  --> We know 'foo' must be 'char[]'
				int argIndex = invoke.getArguments().indexOf(origin);
				usageType = inferExpectedTypeForArgument(invoke, argIndex);
			} else if (parent instanceof LambdaExpressionModel lambda) {
				Model lambdaParent = lambda.getParent();
				// Example:
				//   Usage: passValueToConsumer(t -> op(t), myTValue);
				//   Defin: passValueToConsumer(Supplier, T)
				// Then we know T maps to Object.
				// If the parameter was IntSupplier, T would be int.
				if (lambdaParent instanceof MethodInvocationExpressionModel parentInvoke && resolveMember(parentInvoke) instanceof MethodResolution lambdaReceiverResolution) {
					int lambdaExprArgIndex = parentInvoke.getArguments().indexOf(lambda);
					List<GenericType> parameterTypes = Resolutions.getResolvedMethodParameterGenericTypes(lambdaReceiverResolution);
					if (lambdaExprArgIndex < parameterTypes.size() &&
							GenericTypes.asClassType(parameterTypes.get(lambdaExprArgIndex), jlObjectEntry) instanceof GenericType.ClassType lambdaType) {
						int lambdaArgIndex = lambda.getParameters().indexOf(origin);
						MethodResolution abstractMethod = resolveFunctionalInterfaceMethod(lambdaType);
						if (abstractMethod != null) {
							List<GenericType> abstractParameterTypes = Resolutions.getResolvedMethodParameterGenericTypes(abstractMethod);
							if (lambdaArgIndex < abstractParameterTypes.size())
								usageType = abstractParameterTypes.get(lambdaArgIndex).asDescribable();
						}
					}
				}
				// Example: Consumer c = t -> op(t);
				// Then we know 't' maps to 'Object'
				// Example: IntConsumer c = t -> op(t);
				// But here we know 't' maps to 'int'
				else if (lambdaParent instanceof VariableModel model && resolveType(model.getType()) instanceof ClassResolution lambdaTypeResolution) {
					GenericType.ClassType lambdaType = Resolutions.getResolvedClassType(lambdaTypeResolution);
					int lambdaArgIndex = lambda.getParameters().indexOf(origin);
					MethodResolution abstractMethod = resolveFunctionalInterfaceMethod(lambdaType);
					if (abstractMethod != null) {
						List<GenericType> abstractParameterTypes = Resolutions.getResolvedMethodParameterGenericTypes(abstractMethod);
						if (lambdaArgIndex < abstractParameterTypes.size())
							usageType = abstractParameterTypes.get(lambdaArgIndex).asDescribable();
					}
				}
			}

			// We may know the required type is a lambda, but then that means we may want to adapt the result
			// to mirror the return type the lambda outlines, rather than the class itself.
			//
			// Consumer known = Unknown::foo --> We know 'foo' must be '()V'
			if (adaptLambdaUsage && usageType instanceof ClassEntry classUsage && origin instanceof MethodReferenceExpressionModel) {
				// The origin model being a reference implies the usage type can be a lambda.
				// It should be an interface with a single abstract method.
				MethodResolution abstractMethod = resolveFunctionalInterfaceMethod(GenericTypes.ofClass(classUsage));
				if (abstractMethod != null) {
					DescribableEntry lambdaReturnType = abstractMethod.getResolvedReturnType();

					// Void-compatible method references may still target a non-void member whose
					// result is ignored, so only keep a return-type hint when it constrains anything.
					usageType = lambdaReturnType == VOID ? null : lambdaReturnType;
				} else
					// Reset to null, we do not to incorrectly infer the wrong type for lambdas.
					usageType = null;
			}

			return usageType;
		} finally {
			usageInferenceInProgress.remove(origin);
		}
	}

	@Nullable
	private DescribableEntry inferExpectedTypeForArgument(@Nonnull MethodInvocationExpressionModel invoke, int argumentIndex) {
		if (argumentIndex < 0)
			return null;

		Set<Integer> activeArgumentIndices = argumentInferenceInProgress.computeIfAbsent(invoke, ignored -> new HashSet<>());
		if (!activeArgumentIndices.add(argumentIndex))
			return null;

		try {
			// Extract the method invocation receiver + name.
			String methodName = invoke.getMethodName();
			Model methodReceiver = Objects.requireNonNullElse(invoke.getReceiver(), invoke.getParentOfType(ClassModel.class));
			Resolution receiverResolution = methodReceiver.resolve(this);
			return inferExpectedTypeForArgument(invoke, receiverResolution, methodName, argumentIndex);
		} finally {
			activeArgumentIndices.remove(argumentIndex);
			if (activeArgumentIndices.isEmpty())
				argumentInferenceInProgress.remove(invoke);
		}
	}

	@Nullable
	private DescribableEntry inferExpectedTypeForArgument(@Nonnull MethodInvocationExpressionModel invoke,
	                                                      @Nonnull Resolution methodReceiver,
	                                                      @Nonnull String methodName,
	                                                      int argumentIndex) {
		GenericType receiverType = getResolvedGenericType(methodReceiver);
		GenericType.ClassType receiverClass = receiverType == null ? null : GenericTypes.asClassType(receiverType, jlObjectEntry);
		if (receiverClass == null)
			return null;

		// Reuse already-resolved sibling arguments to narrow method-level type variables.
		List<GenericType> argumentHints = collectKnownGenericArgumentHints(invoke, argumentIndex);
		int argumentCount = invoke.getArguments().size();

		// Collect all methods with the same name in the receiver type's hierarchy.
		List<MethodResolution> candidates = new ArrayList<>();
		visitBoundHierarchy(receiverClass, owner -> owner.classEntry().getDeclaredMethodsByName(methodName).stream()
				.filter(methodEntry -> methodEntry.isVarargs() ?
						argumentCount >= methodEntry.getGenericParameterTypes().size() - 1 :
						argumentCount == methodEntry.getGenericParameterTypes().size())
				.map(methodEntry -> adaptMethodResolution(owner, methodEntry, null, argumentHints))
				.forEach(candidates::add));
		if (candidates.isEmpty())
			return null;

		// Collect parameter types at the given index from all candidates.
		List<DescribableEntry> expectedTypes = new ArrayList<>();
		for (MethodResolution candidate : candidates) {
			List<GenericType> parameterTypes = Resolutions.getResolvedMethodParameterGenericTypes(candidate);
			if (argumentIndex < parameterTypes.size()) {
				expectedTypes.add(parameterTypes.get(argumentIndex).asDescribable());
			} else if (candidate.getMethodEntry().isVarargs() && argumentIndex >= parameterTypes.size() - 1) {
				// Varargs: last parameter applies to all remaining args
				GenericType usableVarargType = GenericTypes.toUsableType(parameterTypes.getLast(), jlObjectEntry);
				if (usableVarargType instanceof GenericType.ArrayType arrayType)
					expectedTypes.add(arrayType.elementType().asDescribable());
			}
		}
		if (expectedTypes.isEmpty())
			return null;

		// Merge possible argument types to common parent.
		DescribableEntry common = expectedTypes.stream()
				.reduce(this::getCommonDescriptor)
				.orElse(expectedTypes.getFirst());
		if (expectedTypes.size() > 1 && common instanceof ClassEntry commonClass) {
			List<ClassEntry> expectedClasses = expectedTypes.stream()
					.filter(t -> t instanceof ClassEntry)
					.map(ClassEntry.class::cast).toList();
			return new MultiClassEntry(expectedClasses, commonClass);
		}
		return common;
	}

	@Nonnull
	private MethodResolution adaptMethodResolution(@Nonnull GenericType.ClassType ownerType, @Nonnull MethodEntry methodEntry,
	                                               @Nullable GenericType returnTypeHint,
	                                               @Nullable List<GenericType> argumentTypeHints) {
		// Start with receiver bindings, then layer on method-level bindings inferred from
		// the expected return type and any already-known argument types.
		Map<GenericTypeParameter, GenericType> bindings = new LinkedHashMap<>(GenericTypes.bind(ownerType));
		GenericType resolvedReturnType = GenericTypes.substitute(methodEntry.getGenericReturnType(), bindings);
		if (returnTypeHint != null)
			collectGenericTypeBindings(resolvedReturnType, returnTypeHint, bindings);
		if (argumentTypeHints != null && !argumentTypeHints.isEmpty()) {
			List<GenericType> parameterTypes = methodEntry.getGenericParameterTypes();
			int paramCount = parameterTypes.size();
			for (int i = 0; i < argumentTypeHints.size(); i++) {
				GenericType argumentTypeHint = argumentTypeHints.get(i);
				if (argumentTypeHint == null)
					continue;

				GenericType parameterType;
				if (methodEntry.isVarargs() && i >= paramCount - 1) {
					GenericType varargType = GenericTypes.substitute(parameterTypes.getLast(), bindings);
					parameterType = varargType instanceof GenericType.ArrayType arrayType ? arrayType.elementType() : varargType;
				} else if (i < paramCount) {
					parameterType = GenericTypes.substitute(parameterTypes.get(i), bindings);
				} else {
					continue;
				}

				collectGenericTypeBindings(parameterType, argumentTypeHint, bindings);
			}
		}
		return adaptMethodResolution(ownerType, methodEntry, bindings);
	}

	private void collectGenericTypeBindings(@Nonnull GenericType expectedType, @Nullable GenericType actualType,
	                                        @Nonnull Map<GenericTypeParameter, GenericType> bindings) {
		GenericType usableActualType = GenericTypes.toUsableType(actualType, jlObjectEntry);
		if (usableActualType == null)
			return;

		// Walk the generic shape in parallel so a parameter like Consumer<T> can learn
		// that T is String from a concrete argument like Consumer<String>.
		switch (expectedType) {
			case GenericType.TypeVariableType typeVariableType -> bindings.put(typeVariableType.parameter(), usableActualType);
			case GenericType.ArrayType expectedArrayType when usableActualType instanceof GenericType.ArrayType actualArrayType -> {
				if (expectedArrayType.dimensions() == actualArrayType.dimensions())
					collectGenericTypeBindings(expectedArrayType.elementType(), actualArrayType.elementType(), bindings);
			}
			case GenericType.ClassType expectedClassType when usableActualType instanceof GenericType.ClassType actualClassType -> {
				GenericType.ClassType actualOwnerType = actualClassType.classEntry().getName().equals(expectedClassType.classEntry().getName()) ?
						actualClassType : GenericTypes.adaptToOwner(actualClassType, expectedClassType.classEntry(), jlObjectEntry);
				if (actualOwnerType == null)
					return;
				int typeArgCount = Math.min(expectedClassType.typeArguments().size(), actualOwnerType.typeArguments().size());
				for (int i = 0; i < typeArgCount; i++)
					collectGenericTypeBindings(expectedClassType.typeArguments().get(i), actualOwnerType.typeArguments().get(i), bindings);
			}
			case GenericType.WildcardType wildcardType -> {
				if (wildcardType.upperBound() != null)
					collectGenericTypeBindings(wildcardType.upperBound(), usableActualType, bindings);
			}
			default -> {
			}
		}
	}

	@Nullable
	private List<GenericType> collectKnownGenericArgumentHints(@Nonnull MethodInvocationExpressionModel invoke, int ignoredArgumentIndex) {
		List<AbstractExpressionModel> arguments = invoke.getArguments();
		if (arguments.isEmpty())
			return Collections.emptyList();

		// Leave the argument being inferred blank, but keep any sibling argument types
		// so overload selection can still bind method type variables from them.
		List<GenericType> hints = new ArrayList<>(arguments.size());
		boolean anyKnown = false;
		for (int i = 0; i < arguments.size(); i++) {
			GenericType argumentType = null;
			if (i != ignoredArgumentIndex)
				argumentType = getResolvedGenericType(arguments.get(i).resolve(this));
			if (argumentType != null)
				anyKnown = true;
			hints.add(argumentType);
		}
		return anyKnown ? hints : null;
	}

	@Nonnull
	private DescribableEntry getCommonDescriptor(@Nonnull DescribableEntry a, @Nonnull DescribableEntry b) {
		// Classes -> Common parent
		// Primitive -> Widest type
		// Arrays -> Common element type
		// Anything else ------> Object
		return switch (a) {
			case ClassEntry ca when b instanceof ClassEntry cb -> ca.getCommonParent(cb);
			case PrimitiveEntry pa when b instanceof PrimitiveEntry pb -> pa.isAssignableFrom(pb) ? pa : pb;
			case ArrayEntry aa when b instanceof ArrayEntry ab && aa.getDimensions() == ab.getDimensions() ->
					getCommonDescriptor(aa.getElementEntry(), ab.getElementEntry()).toArrayEntry(aa.getDimensions());
			default -> jlObjectEntry;
		};
	}

	@Nonnull
	private Resolution resolveFieldInContext(@Nonnull Resolution contextResolution, @Nonnull Model origin,
	                                         @Nonnull String fieldName) {
		DescribableEntry typeHint = inferFromUsage(origin, true);
		return resolveFieldInContext(contextResolution, fieldName, typeHint);
	}

	@Nonnull
	private Resolution resolveMethodInContext(@Nonnull Resolution contextResolution, @Nonnull Model origin,
	                                          @Nonnull String methodName) {
		// Try to resolve the implied method return type based on the use case of the selection.
		DescribableEntry returnType = methodName.startsWith("<") ? VOID : inferFromUsage(origin, true);

		// Resolve the method's arguments.
		List<GenericType> genericArguments = collectGenericMethodArgumentsInParentContext(origin);
		List<DescribableEntry> describableArguments = collectMethodArgumentsInParentContext(origin);

		// Member selection is the method identifier
		if (contextResolution instanceof ClassResolution classResolution) {
			Resolution resolution = resolveMethodByNameInClass(Resolutions.getResolvedClassType(classResolution), methodName,
					rawGenericType(returnType), genericArguments, describableArguments);

			// Unbound references like String::trim use the SAM's first parameter as the receiver,
			// so retry once with that synthetic receiver slot removed.
			if (!resolution.isUnknown() || !isUnboundTypeMethodReference(origin) || genericArguments == null || genericArguments.isEmpty())
				return resolution;
			return resolveMethodByNameInClass(Resolutions.getResolvedClassType(classResolution), methodName,
					rawGenericType(returnType), genericArguments.subList(1, genericArguments.size()),
					describableArguments == null ? null : describableArguments.subList(1, describableArguments.size()));
		} else if (contextResolution instanceof FieldResolution fieldResolution) {
			GenericType fieldType = Resolutions.getResolvedFieldGenericType(fieldResolution);
			GenericType usableFieldType = GenericTypes.toUsableType(fieldType, jlObjectEntry);
			if (usableFieldType instanceof GenericType.ClassType declaringClass)
				return resolveMethodByNameInClass(declaringClass, methodName,
						rawGenericType(returnType), genericArguments, describableArguments);
		} else if (contextResolution instanceof VariableResolution variableResolution) {
			GenericType variableType = Resolutions.getResolvedVariableGenericType(variableResolution);
			GenericType usableVariableType = GenericTypes.toUsableType(variableType, jlObjectEntry);
			if (usableVariableType instanceof GenericType.ClassType declaringClass)
				return resolveMethodByNameInClass(declaringClass, methodName,
						rawGenericType(returnType), genericArguments, describableArguments);
			else if (usableVariableType != null && usableVariableType.asDescribable() instanceof ArrayEntry)
				return resolveMethodByNameInClass(GenericTypes.ofClass(jlObjectEntry), methodName,
						rawGenericType(returnType), genericArguments, describableArguments);
		} else if (contextResolution instanceof MethodResolution methodResolution) {
			GenericType methodReturnType = Resolutions.getResolvedMethodReturnGenericType(methodResolution);
			GenericType usableReturnType = GenericTypes.toUsableType(methodReturnType, jlObjectEntry);
			if (usableReturnType instanceof GenericType.ClassType declaringClass)
				return resolveMethodByNameInClass(declaringClass, methodName,
						rawGenericType(returnType), genericArguments, describableArguments);
		} else if (contextResolution instanceof ArrayResolution) {
			return resolveMethodByNameInClass(GenericTypes.ofClass(jlObjectEntry), methodName,
					rawGenericType(returnType), genericArguments, describableArguments);
		}

		return unknown();
	}

	@Nonnull
	private Resolution resolveMethodReturnType(@Nonnull MethodInvocationExpressionModel methodInvocation) {
		return toValueTypeResolution(resolveMember(methodInvocation));
	}

	@Nonnull
	private Resolution resolveSwitchExpression(@Nonnull SwitchExpressionModel switchExpr) {
		List<Resolution> caseResolutions = switchExpr.getCases().stream()
				.map(m -> {
					Collection<? extends Model> models = m.getBody() != null ? Collections.singletonList(m.getBody()) : m.getExpressions();
					for (Model model : models) {
						// If the model is an expression, it *should* be resolvable to a type.
						if (model instanceof AbstractExpressionModel) {
							Resolution expressionResolution = model.resolve(this);
							if (!expressionResolution.isUnknown())
								return expressionResolution;
						}

						// Attempt to resolve what the yielded value will be.
						List<YieldStatementModel> yieldChildren = model.getRecursiveChildrenOfType(YieldStatementModel.class);
						for (YieldStatementModel yieldChild : yieldChildren) {
							Resolution resolvedYieldValueType = yieldChild.resolve(this);
							if (!resolvedYieldValueType.isUnknown())
								return resolvedYieldValueType;
						}

						// Otherwise if there are no yields, then check and see if an exception is thrown.
						// This is a common case for default branches, and we'll use a special resolution to indicate
						// that this path will always throw.
						List<ThrowStatementModel> throwsChildren = model.getRecursiveChildrenOfType(ThrowStatementModel.class);
						if (!throwsChildren.isEmpty())
							return throwing();
					}

					// Case couldn't be resolved.
					return unknown();
				})
				.filter(r -> !(r instanceof ThrowingResolution || r instanceof NullResolution))
				.map(this::toValueTypeResolution)
				.toList();

		// If we have no cases or any case is strictly an unknown resolution then we cannot resolve the yielded type.
		if (caseResolutions.isEmpty() || caseResolutions.stream().anyMatch(Resolution::isUnknown))
			return unknown();

		// Find the common type/resolution.
		return caseResolutions.stream()
				.reduce(Resolutions::mergeWith)
				.orElse(unknown());
	}

	@Nonnull
	private Resolution resolveBinaryExpression(@Nonnull BinaryExpressionModel binary) {
		return switch (binary.getOperator()) {
			case PLUS -> mergeWith(ADDITION_OR_CONCAT, binary.getLeft().resolve(this), binary.getRight().resolve(this));
			case MINUS, MULTIPLY, DIVIDE, REMAINDER,
			     BIT_OR, BIT_AND, BIT_XOR,
			     SHIFT_LEFT, SHIFT_RIGHT, SHIFT_RIGHT_UNSIGNED ->
					mergeWith(binary.getLeft().resolve(this), binary.getRight().resolve(this));
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
		List<GenericType> genericArguments = collectGenericMethodArgumentsInParentContext(origin);
		if (genericArguments == null)
			return null;
		return genericArguments.stream()
				.map(type -> GenericTypes.toUsableType(type, jlObjectEntry))
				.map(type -> type == null ? null : type.asDescribable())
				.toList();
	}

	@Nullable
	private List<GenericType> collectGenericMethodArgumentsInParentContext(@Nonnull Model origin) {
		// For method references, find the matching single-abstract-method interface method, then extract the parameters.
		if (origin instanceof MethodReferenceExpressionModel) {
			// Places to consider for argument inference:
			//  - Variable type where the reference is stored
			//     - Supplier<Box> supplier = Box::new
			//  - Variable type where the reference is passed to
			//     - called as a parameter to build(Supplier<Box>) or build(Function<T, Box>)
			DescribableEntry usageType = inferFromUsage(origin, false);
			if (usageType instanceof ClassEntry classUsage) {
				// The origin model being a reference implies the usage type can be a lambda.
				// It should be an interface with a single abstract method.
				MethodResolution abstractMethod = resolveFunctionalInterfaceMethod(GenericTypes.ofClass(classUsage));
				if (abstractMethod != null)
					// Preserve the SAM parameter generics so later method selection can distinguish
					// cases like Consumer<String> from raw Consumer.
					return Resolutions.getResolvedMethodParameterGenericTypes(abstractMethod);
			}

			return null;
		}

		MethodInvocationExpressionModel methodInvocation = origin instanceof MethodInvocationExpressionModel invoke
				? invoke : origin.getParentOfType(MethodInvocationExpressionModel.class);
		if (methodInvocation == null)
			return null;

		List<AbstractExpressionModel> arguments = methodInvocation.getArguments();
		List<GenericType> genericArguments = arguments.isEmpty() ? Collections.emptyList() : new ArrayList<>(arguments.size());
		for (int i = 0; i < arguments.size(); i++) {
			AbstractExpressionModel argument = arguments.get(i);
			Resolution resolution = argument.resolve(this);
			GenericType argumentType = getResolvedGenericType(resolution);
			if (argumentType != null) {
				genericArguments.add(argumentType);
			} else {
				// See if we can infer the type based on matching possible argument types for methods of the same name.
				DescribableEntry inferredEntry = inferExpectedTypeForArgument(methodInvocation, i);
				if (inferredEntry == null) {
					// Preserve the argument count even when this argument's type is unknown,
					// so overloads can still be differentiated by arity.
					genericArguments.add(null);
					continue;
				}
				genericArguments.add(rawGenericType(inferredEntry));
			}
		}

		return genericArguments;
	}

	@Nullable
	private List<GenericType> toGenericTypeHints(@Nullable List<? extends DescribableEntry> argumentTypeEntries) {
		if (argumentTypeEntries == null)
			return null;
		List<GenericType> genericTypes = new ArrayList<>(argumentTypeEntries.size());
		for (DescribableEntry argumentTypeEntry : argumentTypeEntries)
			genericTypes.add(rawGenericType(argumentTypeEntry));
		return genericTypes;
	}

	private boolean isUnboundTypeMethodReference(@Nonnull Model origin) {
		if (!(origin instanceof MethodReferenceExpressionModel methodReference) || methodReference.getMode() != MethodReferenceExpressionModel.Mode.INVOKE)
			return false;
		if (!(methodReference.getQualifier() instanceof NamedModel namedQualifier))
			return false;
		return resolveNamed(namedQualifier) instanceof ClassResolution;
	}

	/**
	 * Partial member selection that can be used to resolve either a field or method depending on the context.
	 *
	 * @param baseText
	 * 		The base text of the selection, such as {@code Constants.MY_CONST} or {@code Constants.getMyConst()}.
	 * 		This is used for error reporting and should include any qualifiers but not method arguments.
	 * @param memberName
	 * 		The member name being selected, such as {@code MY_CONST} or {@code getMyConst}.
	 */
	private record AccessFragment(@Nonnull String baseText, @Nonnull String memberName) {}

	/**
	 * Type of member to resolve in a selection context.
	 */
	private enum MemberTarget {
		FIELDS, METHODS
	}
}
