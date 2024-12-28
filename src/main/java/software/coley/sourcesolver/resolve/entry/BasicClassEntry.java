package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class BasicClassEntry implements ClassEntry {
	private final String className;
	private final int access;
	private final ClassEntry superEntry;
	private final List<ClassEntry> interfaceEntries;
	private final List<ClassEntry> innerClassEntries;
	private final List<FieldEntry> fields;
	private final List<MethodEntry> methods;

	public BasicClassEntry(@Nonnull String className, int access,
	                       @Nullable ClassEntry superEntry,
	                       @Nonnull List<ClassEntry> interfaceEntries,
						   @Nonnull List<ClassEntry> innerClassEntries,
	                       @Nonnull List<FieldEntry> fields,
	                       @Nonnull List<MethodEntry> methods) {
		this.className = className;
		this.access = access;
		this.superEntry = superEntry;
		this.interfaceEntries = interfaceEntries;
		this.innerClassEntries = innerClassEntries;
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

	@Nonnull
	@Override
	public List<FieldEntry> getFields() {
		return fields;
	}

	@Nonnull
	@Override
	public List<MethodEntry> getMethods() {
		return methods;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BasicClassEntry that = (BasicClassEntry) o;

		if (access != that.access) return false;
		if (!className.equals(that.className)) return false;
		if (!Objects.equals(superEntry, that.superEntry)) return false;
		if (!interfaceEntries.equals(that.interfaceEntries)) return false;
		if (!innerClassEntries.equals(that.innerClassEntries)) return false;
		if (!fields.equals(that.fields)) return false;
		return methods.equals(that.methods);
	}

	@Override
	public int hashCode() {
		int result = className.hashCode();
		result = 31 * result + access;
		result = 31 * result + (superEntry != null ? superEntry.hashCode() : 0);
		result = 31 * result + interfaceEntries.hashCode();
		result = 31 * result + innerClassEntries.hashCode();
		result = 31 * result + fields.hashCode();
		result = 31 * result + methods.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return getName();
	}
}
