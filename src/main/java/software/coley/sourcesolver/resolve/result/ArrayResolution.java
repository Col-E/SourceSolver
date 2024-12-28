package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ArrayEntry;

import javax.annotation.Nonnull;

/**
 * Resolution of an array.
 *
 * @author Matt Coley
 */
non-sealed public interface ArrayResolution extends DescribableResolution {
	/**
	 * @return The resolved array type.
	 */
	@Nonnull
	ArrayEntry getArrayEntry();

	/**
	 * @return Number of dimensions in the resolved array type.
	 */
	default int getDimensions() {
		return getArrayEntry().getDimensions();
	}

	/**
	 * @return Resolution of the array's element type.
	 */
	@Nonnull
	DescribableResolution getElementTypeResolution();

	@Nonnull
	@Override
	default ArrayEntry getDescribableEntry() {
		return getArrayEntry();
	}
}
