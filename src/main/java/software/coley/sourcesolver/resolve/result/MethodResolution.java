package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.MethodEntry;

import jakarta.annotation.Nonnull;

/**
 * Resolution of a method.
 *
 * @author Matt Coley
 */
non-sealed public interface MethodResolution extends MemberResolution {
	/**
	 * @return The resolved method.
	 */
	@Nonnull
	MethodEntry getMethodEntry();

	@Nonnull
	@Override
	default MethodEntry getDescribableEntry() {
		return getMethodEntry();
	}
}
