package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

/**
 * Metadata model of an array type.
 *
 * @author Matt Coley
 */
public non-sealed interface ArrayEntry extends DescribableEntry {
	/**
	 * @return Dimensions of the array.
	 */
	int getDimensions();

	/**
	 * @return Element type of the array.
	 */
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
