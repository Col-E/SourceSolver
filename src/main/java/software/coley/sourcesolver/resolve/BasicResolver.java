package software.coley.sourcesolver.resolve;

import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.ClassModel;
import software.coley.sourcesolver.model.CompilationUnitModel;
import software.coley.sourcesolver.model.ImportModel;
import software.coley.sourcesolver.model.MethodModel;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.model.PackageModel;
import software.coley.sourcesolver.model.VariableModel;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.EntryPool;
import software.coley.sourcesolver.resolve.entry.FieldEntry;
import software.coley.sourcesolver.resolve.entry.MethodEntry;
import software.coley.sourcesolver.resolve.result.ClassResolution;
import software.coley.sourcesolver.resolve.result.DescribableResolution;
import software.coley.sourcesolver.resolve.result.FieldResolution;
import software.coley.sourcesolver.resolve.result.MethodResolution;
import software.coley.sourcesolver.resolve.result.PackageResolution;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.resolve.result.Resolutions;
import software.coley.sourcesolver.resolve.result.UnknownResolution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BasicResolver implements Resolver {
	private final CompilationUnitModel unit;
	private final EntryPool pool;

	public BasicResolver(@Nonnull CompilationUnitModel unit, @Nonnull EntryPool pool) {
		this.unit = unit;
		this.pool = pool;
	}

	@Nonnull
	@Override
	public Resolution resolveAt(int index, @Nullable AbstractModel target) {
		List<AbstractModel> modelPath = new ArrayList<>();
		AbstractModel model = unit;
		while (model != null) {
			modelPath.add(model);
			if (model == target)
				break;
			model = model.getChildAtPosition(index);
		}
		return resolve(modelPath);
	}

	@Nonnull
	private Resolution resolve(@Nonnull List<AbstractModel> modelPath) {
		if (modelPath.isEmpty())
			return UnknownResolution.INSTANCE;

		// TODO: Do more than these basic cases
		//  So far:
		//   - imports
		//   - class declarations
		//      - but not annotations, type parameters, etc
		//   - field declarations
		//      - but no child nodes
		//   - method declarations
		//      - but no child nodes
		AbstractModel tail = modelPath.get(modelPath.size() - 1);
		if (tail instanceof ClassModel clazz) {
			return resolveClassModel(clazz);
		} else if (tail instanceof MethodModel method) {
			return resolveMethodModel(method);
		} else if (tail instanceof VariableModel variable
				&& tail.getParent() instanceof ClassModel declaringClass) {
			return resolveFieldModel(declaringClass, variable);
		} else if (tail instanceof PackageModel pkg) {
			return resolvePackageModel(pkg);
		} else if (tail instanceof ImportModel imp) {
			return resolveImportModel(imp);
		} else if (tail instanceof ModifiersModel modifiers
				&& modifiers.getParent() instanceof MethodModel method
				&& method.getName().equals("<clinit>")) {
			return resolveStaticInitializer(method);
		}

		return UnknownResolution.INSTANCE;
	}

	@Nonnull
	private Resolution resolvePackageModel(@Nonnull PackageModel pkg) {
		String packageName = pkg.isDefaultPackage() ? null : pkg.getName().replace('.', '/');
		return (PackageResolution) () -> packageName;
	}

	@Nonnull
	private Resolution resolveImportModel(@Nonnull ImportModel imp) {
		if (imp.isStatic()) {
			String name = imp.getName();
			if (name.lastIndexOf('*') > 0)
				return UnknownResolution.INSTANCE;
			int lastDot = name.lastIndexOf('.');
			String memberName = name.substring(lastDot + 1);
			name = name.substring(0, lastDot);
			if (resolveDotName(name) instanceof ClassResolution declaringClassResolution) {
				ClassEntry declaringClassEntry = declaringClassResolution.getClassEntry();
				Collection<FieldEntry> fieldsByName = declaringClassEntry.getDistinctFieldsByNameInHierarchy(memberName).values();
				if (!fieldsByName.isEmpty())
					return Resolutions.ofField(declaringClassEntry, fieldsByName.iterator().next());
				Collection<MethodEntry> methodsByName = declaringClassEntry.getDistinctMethodsByNameInHierarchy(memberName).values();
				if (!methodsByName.isEmpty())
					return Resolutions.ofMethod(declaringClassEntry, methodsByName.iterator().next());
			}
			return UnknownResolution.INSTANCE;
		}
		return resolveDotName(imp.getName());
	}

	@Nonnull
	private Resolution resolveDotName(@Nonnull String name) {
		name = name.replace('.', '/');
		Resolution resolution = Resolutions.ofClass(pool, name);
		while (resolution instanceof UnknownResolution && name.indexOf('/') >= 0) {
			int lastSlash = name.lastIndexOf('/');
			String tail = name.substring(lastSlash) + 1;
			name = name.substring(0, lastSlash) + '$' + tail;
		}
		return resolution;
	}

	@Nonnull
	private Resolution resolveClassModel(@Nonnull ClassModel clazz) {
		PackageResolution pkg = (PackageResolution) unit.getPackage().resolve(this);
		String name = clazz.getName();
		if (pkg.isDefaultPackage())
			return Resolutions.ofClass(pool, name);
		return Resolutions.ofClass(pool, pkg.getPackageName() + '/' + name);
	}

	@Nonnull
	private Resolution resolveFieldModel(@Nonnull ClassModel definingClass, @Nonnull VariableModel field) {
		// Skip if parent context cannot be resolved.
		if (!(definingClass.resolve(this) instanceof ClassResolution resolvedDefiningClass))
			return UnknownResolution.INSTANCE;

		// Check and see if we can take a shortcut by just doing a name lookup.
		String fieldName = field.getName();
		ClassEntry definingClassEntry = resolvedDefiningClass.getClassEntry();
		if (resolveFieldByName(definingClassEntry, fieldName) instanceof FieldResolution resolution)
			return resolution;

		// Can't take a shortcut, we need to resolve the descriptor then look up with that.
		if (!(field.getType().resolve(this) instanceof DescribableResolution resolvedType))
			return UnknownResolution.INSTANCE;

		// Resolve by name/descriptor.
		return Resolutions.ofField(definingClassEntry, fieldName, resolvedType.getDescribableEntry().getDescriptor());
	}

	@Nonnull
	private Resolution resolveMethodModel(@Nonnull MethodModel method) {
		// Skip if parent context cannot be resolved.
		if (!(method.getParent() instanceof ClassModel definingClass))
			return UnknownResolution.INSTANCE;
		if (!(definingClass.resolve(this) instanceof ClassResolution resolvedDefiningClass))
			return UnknownResolution.INSTANCE;

		// Check and see if we can take a shortcut by just doing a name lookup.
		String methodName = method.getName();
		ClassEntry definingClassEntry = resolvedDefiningClass.getClassEntry();
		if (resolveMethodByName(definingClassEntry, methodName) instanceof MethodResolution resolution)
			return resolution;

		// Can't take a shortcut, we need to resolve the descriptor then look up with that.
		if (!(method.getReturnType().resolve(this) instanceof DescribableResolution resolvedReturnType))
			return UnknownResolution.INSTANCE;
		List<VariableModel> parameters = method.getParameters();
		List<DescribableResolution> resolvedParameterTypes = new ArrayList<>(parameters.size());
		for (VariableModel parameter : parameters) {
			Resolution resolution = parameter.resolve(this);
			if (resolution instanceof DescribableResolution resolvedParameter)
				resolvedParameterTypes.add(resolvedParameter);
			else
				// If a parameter is not resolvable, we cannot resolve this method
				return UnknownResolution.INSTANCE;
		}

		// Resolve by name/descriptor.
		return Resolutions.ofMethod(definingClassEntry, methodName, resolvedReturnType.getDescribableEntry(),
				resolvedParameterTypes.stream().map(DescribableResolution::getDescribableEntry).toList());
	}

	@Nonnull
	private static Resolution resolveFieldByName(@Nonnull ClassEntry classEntry, @Nonnull String fieldName) {
		// Check if the field is declared in this class, and is unique in the hierarchy in terms of signature.
		List<FieldEntry> fieldsByName = classEntry.getFieldsByName(fieldName);
		if (fieldsByName.size() == 1) {
			Map<String, FieldEntry> fieldsByNameInHierarchy = classEntry.getDistinctFieldsByNameInHierarchy(fieldName);
			if (fieldsByNameInHierarchy.size() == 1)
				return Resolutions.ofField(classEntry, fieldsByNameInHierarchy.values().iterator().next());
		}

		// Check in super-type.
		if (classEntry.getSuperEntry() != null
				&& resolveFieldByName(classEntry.getSuperEntry(), fieldName) instanceof FieldResolution resolution)
			return resolution;

		// Check in interfaces.
		for (ClassEntry implementedEntry : classEntry.getImplementedEntries())
			if (resolveFieldByName(implementedEntry, fieldName) instanceof FieldResolution resolution)
				return resolution;

		return UnknownResolution.INSTANCE;
	}

	@Nonnull
	private static Resolution resolveMethodByName(@Nonnull ClassEntry classEntry, @Nonnull String methodName) {
		// Check if the method is declared in this class, and is unique in the hierarchy in terms of signature.
		List<MethodEntry> methodsByName = classEntry.getMethodsByName(methodName);
		if (methodsByName.size() == 1) {
			Map<String, MethodEntry> methodsByNameInHierarchy = classEntry.getDistinctMethodsByNameInHierarchy(methodName);
			if (methodsByNameInHierarchy.size() == 1)
				return Resolutions.ofMethod(classEntry, methodsByNameInHierarchy.values().iterator().next());
		}

		// Check in super-type.
		if (classEntry.getSuperEntry() != null
				&& resolveMethodByName(classEntry.getSuperEntry(), methodName) instanceof MethodResolution resolution)
			return resolution;

		// Check in interfaces.
		for (ClassEntry implementedEntry : classEntry.getImplementedEntries())
			if (resolveMethodByName(implementedEntry, methodName) instanceof MethodResolution resolution)
				return resolution;

		return UnknownResolution.INSTANCE;
	}

	@Nonnull
	private Resolution resolveStaticInitializer(@Nonnull MethodModel method) {
		// Skip if parent context cannot be resolved.
		if (!(method.getParent() instanceof ClassModel definingClass))
			return UnknownResolution.INSTANCE;
		if (!(definingClass.resolve(this) instanceof ClassResolution resolvedDefiningClass))
			return UnknownResolution.INSTANCE;

		// Static initializers will only be resolved in the target class.
		List<MethodEntry> initializers = resolvedDefiningClass.getClassEntry().getMethodsByName("<clinit>");
		if (initializers.isEmpty())
			return UnknownResolution.INSTANCE;
		return Resolutions.ofMethod(resolvedDefiningClass.getClassEntry(), initializers.get(0));
	}
}
