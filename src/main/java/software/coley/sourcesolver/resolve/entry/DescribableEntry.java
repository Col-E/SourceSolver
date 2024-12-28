package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

public sealed interface DescribableEntry permits ArrayEntry, ClassEntry, PrimitiveEntry, NullEntry, MemberEntry {
	@Nonnull
	String getDescriptor();

	@Nonnull
	default ArrayEntry toArrayEntry(int dimensions) {
		return new BasicArrayEntry(dimensions, this);
	}

	boolean isAssignableFrom(@Nonnull DescribableEntry other);
}
