package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

public interface ArrayEntry extends DescribableEntry {
	@Nonnull
	static ArrayEntry getArray(int dimensions, @Nonnull DescribableEntry element) {
		return new BasicArrayEntry(dimensions, element);
	}

	int getDimensions();

	@Nonnull
	DescribableEntry getElementEntry();

	@Override
	default boolean isAssignableFrom(@Nonnull DescribableEntry other) {
		if (other instanceof NullEntry)
			return true;
		if (other instanceof ArrayEntry otherArray)
			return isAssignableFrom(otherArray);
		return false;
	}

	default boolean isAssignableFrom(@Nonnull ArrayEntry other) {
		return getDimensions() == other.getDimensions()
				&& getElementEntry().isAssignableFrom(other.getElementEntry());
	}

	@Nonnull
	@Override
	default ArrayEntry toArrayEntry(int dimensions) {
		return new BasicArrayEntry(dimensions, getElementEntry());
	}
}
