package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;

import jakarta.annotation.Nonnull;

/**
 * Resolution of a class.
 *
 * @author Matt Coley
 */
non-sealed public interface ClassResolution extends DescribableResolution {
	/**
	 * @return The resolved class type.
	 */
	@Nonnull
	ClassEntry getClassEntry();

	@Nonnull
	@Override
	default DescribableEntry getDescribableEntry() {
		return getClassEntry();
	}
}
