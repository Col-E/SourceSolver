package software.coley.sourcesolver.resolve;

import software.coley.sourcesolver.model.*;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.EntryPool;
import software.coley.sourcesolver.resolve.entry.FieldEntry;
import software.coley.sourcesolver.resolve.entry.MethodEntry;
import software.coley.sourcesolver.resolve.result.ClassResolution;
import software.coley.sourcesolver.resolve.result.DescribableResolution;
import software.coley.sourcesolver.resolve.result.FieldResolution;
import software.coley.sourcesolver.resolve.result.MethodResolution;
import software.coley.sourcesolver.resolve.result.MultiClassResolution;
import software.coley.sourcesolver.resolve.result.PackageResolution;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.resolve.result.UnknownResolution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
		return map;
	}

	@Nonnull
	@Override
	public Resolution resolveAt(int index, @Nullable Model target) {
		if (target != null)
			return resolve(target);

		// Find the deepest model at position.
		List<Model> modelPath = new ArrayList<>();
		Model model = unit;
		while (model != null) {
			modelPath.add(model);
			model = model.getChildAtPosition(index);
		}

		// Resolve off of the deepest model so that it is aware of the results and can cache them.
		return modelPath.get(modelPath.size() - 1).resolve(this);
	}

	@Nonnull
	private Resolution resolve(@Nonnull Model target) {
		// TODO: Do more than these basic cases
		//  So far:
		//   - imports
		//   - class declarations
		//      - but not annotations, type parameters, etc
		//   - field declarations
		//      - but no child nodes
		//   - method declarations
		//      - but no child nodes
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
		} else if (target instanceof NameExpressionModel nameExpression) {
			return resolveNameUsage(nameExpression);
		} else if (target instanceof TypeModel type)
			return resolveType(type);
		else if (target instanceof ModifiersModel)
			return resolve(target.getParent());
		// TODO: Resolve literals (as primitive type, or string type)

		System.err.println(target.getClass().getSimpleName() + " : parent=" + target.getParent().getClass().getSimpleName());
		return unknown();
	}

	@Nonnull
	private Resolution resolveNameUsage(@Nonnull NameExpressionModel nameExpression) {
		Model parent = nameExpression.getParent();

		if (parent instanceof ClassModel)
			return resolveImportedDotName(nameExpression);
		else if (parent instanceof ImplementsModel)
			return resolveImportedDotName(nameExpression);
		else if (parent instanceof TypeModel parentType)
			return resolveType(parentType);

		// TODO: Case for local class name references
		//  - method variables / parameters
		//  - fields
		//  - imported static fields

		return unknown();
	}

	@Nonnull
	private Resolution resolveDotName(@Nonnull String name) {
		name = name.replace('.', '/');
		Resolution resolution = ofClass(pool, name);
		while (resolution instanceof UnknownResolution && name.indexOf('/') >= 0) {
			int lastSlash = name.lastIndexOf('/');
			String tail = name.substring(lastSlash) + 1;
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
			int lastDot = name.lastIndexOf('.');
			String memberName = name.substring(lastDot + 1);
			name = name.substring(0, lastDot);
			if (resolveDotName(name) instanceof ClassResolution declaringClassResolution) {
				// Find the first matching field/method by the give name.
				// Star imports cannot be mapped to any given member.
				if (name.lastIndexOf('*') < 0) {
					ClassEntry declaringClassEntry = declaringClassResolution.getClassEntry();
					Collection<FieldEntry> fieldsByName = declaringClassEntry.getDistinctFieldsByNameInHierarchy(memberName).values();
					if (!fieldsByName.isEmpty())
						return ofField(declaringClassEntry, fieldsByName.iterator().next());
					Collection<MethodEntry> methodsByName = declaringClassEntry.getDistinctMethodsByNameInHierarchy(memberName).values();
					if (!methodsByName.isEmpty())
						return ofMethod(declaringClassEntry, methodsByName.iterator().next());
				}

				// Fall back to just the class resolution.
				return declaringClassResolution;
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
		if (resolveFieldByName(definingClassEntry, fieldName) instanceof FieldResolution resolution)
			return resolution;

		// Can't take a shortcut, we need to resolve the descriptor then look up with that.
		if (!(field.getType().resolve(this) instanceof DescribableResolution resolvedType))
			return unknown();

		// Resolve by name/descriptor.
		return ofField(definingClassEntry, fieldName, resolvedType.getDescribableEntry().getDescriptor());
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
		if (resolveMethodByName(definingClassEntry, methodName) instanceof MethodResolution resolution)
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
	private static Resolution resolveFieldByName(@Nonnull ClassEntry classEntry, @Nonnull String fieldName) {
		// Check if the field is declared in this class, and is unique in the hierarchy in terms of signature.
		List<FieldEntry> fieldsByName = classEntry.getFieldsByName(fieldName);
		if (fieldsByName.size() == 1) {
			Map<String, FieldEntry> fieldsByNameInHierarchy = classEntry.getDistinctFieldsByNameInHierarchy(fieldName);
			if (fieldsByNameInHierarchy.size() == 1)
				return ofField(classEntry, fieldsByNameInHierarchy.values().iterator().next());
		}

		// Check in super-type.
		if (classEntry.getSuperEntry() != null
				&& resolveFieldByName(classEntry.getSuperEntry(), fieldName) instanceof FieldResolution resolution)
			return resolution;

		// Check in interfaces.
		for (ClassEntry implementedEntry : classEntry.getImplementedEntries())
			if (resolveFieldByName(implementedEntry, fieldName) instanceof FieldResolution resolution)
				return resolution;

		return unknown();
	}

	@Nonnull
	private static Resolution resolveMethodByName(@Nonnull ClassEntry classEntry, @Nonnull String methodName) {
		// Check if the method is declared in this class, and is unique in the hierarchy in terms of signature.
		List<MethodEntry> methodsByName = classEntry.getMethodsByName(methodName);
		if (methodsByName.size() == 1) {
			Map<String, MethodEntry> methodsByNameInHierarchy = classEntry.getDistinctMethodsByNameInHierarchy(methodName);
			if (methodsByNameInHierarchy.size() == 1)
				return ofMethod(classEntry, methodsByNameInHierarchy.values().iterator().next());
		}

		// Check in super-type.
		if (classEntry.getSuperEntry() != null
				&& resolveMethodByName(classEntry.getSuperEntry(), methodName) instanceof MethodResolution resolution)
			return resolution;

		// Check in interfaces.
		for (ClassEntry implementedEntry : classEntry.getImplementedEntries())
			if (resolveMethodByName(implementedEntry, methodName) instanceof MethodResolution resolution)
				return resolution;

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
}
