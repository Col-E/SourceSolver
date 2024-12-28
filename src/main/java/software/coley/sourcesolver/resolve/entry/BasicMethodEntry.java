package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

@SuppressWarnings("ClassCanBeRecord")
public class BasicMethodEntry implements MethodEntry {
	private final String name;
	private final String descriptor;
	private final int access;

	public BasicMethodEntry(@Nonnull String name, @Nonnull String descriptor, int access) {
		this.name = name;
		this.descriptor = descriptor;
		this.access = access;
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nonnull
	@Override
	public String getDescriptor() {
		return descriptor;
	}

	@Override
	public int getAccess() {
		return access;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BasicMethodEntry that = (BasicMethodEntry) o;

		if (access != that.access) return false;
		if (!name.equals(that.name)) return false;
		return descriptor.equals(that.descriptor);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + descriptor.hashCode();
		result = 31 * result + access;
		return result;
	}

	@Override
	public String toString() {
		return name + getDescriptor();
	}
}
