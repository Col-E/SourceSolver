package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.PrimitiveEntry;

import jakarta.annotation.Nonnull;

/**
 * Resolution of a primitive type.
 *
 * @author Matt Coley
 */
non-sealed public interface PrimitiveResolution extends DescribableResolution {
	/**
	 * @return The resolved primitive type.
	 */
	@Nonnull
	PrimitiveEntry getPrimitiveEntry();

	@Nonnull
	@Override
	default PrimitiveEntry getDescribableEntry() {
		return getPrimitiveEntry();
	}
}
