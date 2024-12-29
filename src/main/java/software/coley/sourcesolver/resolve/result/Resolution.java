package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;

/**
 * Base type of any resolution.
 */
public sealed interface Resolution permits DescribableResolution, PackageResolution, MultiClassResolution, MultiMemberResolution, ThrowingResolution, UnknownResolution {
	/**
	 * @param other
	 * 		Other resolution to check against.
	 *
	 * @return {@code true} if the other resolution represents the same resolved content.
	 */
	boolean matches(@Nonnull Resolution other);

	/**
	 * @return {@code true} if this resolution has failed.
	 */
	default boolean isUnknown() {
		return false;
	}
}
