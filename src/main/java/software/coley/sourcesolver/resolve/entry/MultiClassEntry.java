package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * Metadata model of a class type that can be one of multiple classes.
 * All model behaviors operate on the common type amongst the multiple classes.
 */
public class MultiClassEntry implements ClassEntry {
	private final List<ClassEntry> classEntries;
	private final ClassEntry commonEntry;

	/**
	 * @param classEntries
	 * 		Multiple classes that this entry can resolve to.
	 * @param commonEntry
	 * 		Common type between the multiple entries.
	 */
	public MultiClassEntry(@Nonnull List<ClassEntry> classEntries, @Nonnull ClassEntry commonEntry) {
		this.classEntries = classEntries;
		this.commonEntry = commonEntry;
	}

	/**
	 * @return Multiple classes that this entry can resolve to.
	 */
	@Nonnull
	public List<ClassEntry> getClassEntries() {
		return classEntries;
	}

	@Nullable
	@Override
	public ClassEntry getSuperEntry() {
		return commonEntry.getSuperEntry();
	}

	@Nonnull
	@Override
	public List<ClassEntry> getImplementedEntries() {
		return commonEntry.getImplementedEntries();
	}

	@Nonnull
	@Override
	public List<ClassEntry> getInnerClassEntries() {
		return commonEntry.getInnerClassEntries();
	}

	@Nullable
	@Override
	public ClassEntry getOuterClass() {
		return commonEntry.getOuterClass();
	}

	@Nonnull
	@Override
	public String getName() {
		return commonEntry.getName();
	}

	@Nonnull
	@Override
	public List<FieldEntry> getDeclaredFields() {
		return commonEntry.getDeclaredFields();
	}

	@Nonnull
	@Override
	public List<MethodEntry> getDeclaredMethods() {
		return commonEntry.getDeclaredMethods();
	}

	@Override
	public int getAccess() {
		return commonEntry.getAccess();
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof MultiClassEntry that)) return false;
		return classEntries.equals(that.classEntries) && commonEntry.equals(that.commonEntry);
	}

	@Override
	public int hashCode() {
		int result = classEntries.hashCode();
		result = 31 * result + commonEntry.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return commonEntry + "[" + classEntries.size() + "]";
	}
}
