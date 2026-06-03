package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.resolve.generic.GenericType;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class BasicMethodEntry implements MethodEntry {
	private final String name;
	private final String descriptor;
	private final int access;
	private final GenericType genericReturnType;
	private final List<GenericType> genericParameterTypes;

	public BasicMethodEntry(@Nonnull String name, @Nonnull String descriptor, int access,
	                        @Nonnull GenericType genericReturnType,
	                        @Nonnull List<GenericType> genericParameterTypes) {
		this.name = name;
		this.descriptor = descriptor;
		this.access = access;
		this.genericReturnType = genericReturnType;
		this.genericParameterTypes = List.copyOf(genericParameterTypes);
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

	@Nonnull
	@Override
	public GenericType getGenericReturnType() {
		return genericReturnType;
	}

	@Nonnull
	@Override
	public List<GenericType> getGenericParameterTypes() {
		return genericParameterTypes;
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
