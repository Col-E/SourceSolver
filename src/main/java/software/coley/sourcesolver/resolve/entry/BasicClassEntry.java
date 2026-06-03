package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.resolve.generic.GenericType;
import software.coley.sourcesolver.resolve.generic.GenericTypeParameter;

import java.util.List;

public class BasicClassEntry implements ClassEntry {
	private final String className;
	private final int access;
	private final ClassEntry superEntry;
	private final List<ClassEntry> interfaceEntries;
	private final List<ClassEntry> innerClassEntries;
	private final ClassEntry outerClass;
	private final List<GenericTypeParameter> typeParameters;
	private final GenericType.ClassType genericSuperType;
	private final List<GenericType.ClassType> genericInterfaceTypes;
	private final List<FieldEntry> fields;
	private final List<MethodEntry> methods;

	public BasicClassEntry(@Nonnull String className, int access,
	                       @Nullable ClassEntry superEntry,
	                       @Nonnull List<ClassEntry> interfaceEntries,
	                       @Nonnull List<ClassEntry> innerClassEntries,
	                       @Nullable ClassEntry outerClass,
	                       @Nonnull List<GenericTypeParameter> typeParameters,
	                       @Nullable GenericType.ClassType genericSuperType,
	                       @Nonnull List<GenericType.ClassType> genericInterfaceTypes,
	                       @Nonnull List<FieldEntry> fields,
	                       @Nonnull List<MethodEntry> methods) {
		this.className = className;
		this.access = access;
		this.superEntry = superEntry;
		this.interfaceEntries = interfaceEntries;
		this.innerClassEntries = innerClassEntries;
		this.outerClass = outerClass;
		this.typeParameters = List.copyOf(typeParameters);
		this.genericSuperType = genericSuperType;
		this.genericInterfaceTypes = List.copyOf(genericInterfaceTypes);
		this.fields = fields;
		this.methods = methods;
	}

	@Nonnull
	@Override
	public String getName() {
		return className;
	}

	@Override
	public int getAccess() {
		return access;
	}

	@Nullable
	@Override
	public ClassEntry getSuperEntry() {
		return superEntry;
	}

	@Nonnull
	@Override
	public List<ClassEntry> getImplementedEntries() {
		return interfaceEntries;
	}

	@Nonnull
	@Override
	public List<ClassEntry> getInnerClassEntries() {
		return innerClassEntries;
	}

	@Nullable
	@Override
	public ClassEntry getOuterClass() {
		return outerClass;
	}

	@Nonnull
	@Override
	public List<GenericTypeParameter> getTypeParameters() {
		return typeParameters;
	}

	@Nullable
	@Override
	public GenericType.ClassType getGenericSuperType() {
		return genericSuperType;
	}

	@Nonnull
	@Override
	public List<GenericType.ClassType> getGenericInterfaceTypes() {
		return genericInterfaceTypes;
	}

	@Nonnull
	@Override
	public List<FieldEntry> getDeclaredFields() {
		return fields;
	}

	@Nonnull
	@Override
	public List<MethodEntry> getDeclaredMethods() {
		return methods;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BasicClassEntry that = (BasicClassEntry) o;

		if (access != that.access) return false;
		return className.equals(that.className);
	}

	@Override
	public int hashCode() {
		int result = className.hashCode();
		result = 31 * result + access;
		return result;
	}

	@Override
	public String toString() {
		return getName();
	}
}
