package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.NullEntry;

import jakarta.annotation.Nonnull;

/**
 * Resolution of a null constant.
 *
 * @author Matt Coley
 */
non-sealed public interface NullResolution extends DescribableResolution {
	@Nonnull
	@Override
	default NullEntry getDescribableEntry() {
		return NullEntry.INSTANCE;
	}
}
