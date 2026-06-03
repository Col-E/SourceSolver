package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.resolve.entry.MethodEntry;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;

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

	/**
	 * @return Resolved return type after applying receiver generic arguments.
	 */
	@Nonnull
	DescribableEntry getResolvedReturnType();

	@Nonnull
	@Override
	default MethodEntry getDescribableEntry() {
		return getMethodEntry();
	}
}
