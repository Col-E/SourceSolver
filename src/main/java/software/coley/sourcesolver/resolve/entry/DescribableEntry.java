package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

/**
 * Metadata model that can be represented with a type descriptor.
 *
 * @author Matt Coley
 */
public sealed interface DescribableEntry permits ArrayEntry, ClassEntry, PrimitiveEntry, NullEntry, MemberEntry {
	/**
	 * @return Type descriptor of this entry.
	 */
	@Nonnull
	String getDescriptor();

	/**
	 * Create an array entry with this entry as the element type.
	 *
	 * @param dimensions
	 * 		Number of dimensions to represent.
	 *
	 * @return Array of the current entry as the element type, with the given number of dimentions.
	 */
	@Nonnull
	default ArrayEntry toArrayEntry(int dimensions) {
		return new BasicArrayEntry(dimensions, this);
	}

	/**
	 * Check if another entry is assignable to the type represented by this entry.
	 *
	 * @param other
	 * 		Some other entry.
	 *
	 * @return {@code true} when the other entry is assignable to the type represented by this entry.
	 */
	boolean isAssignableFrom(@Nonnull DescribableEntry other);
}
