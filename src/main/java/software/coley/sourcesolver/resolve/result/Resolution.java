package software.coley.sourcesolver.resolve.result;

/**
 * Base type of any resolution.
 */
public sealed interface Resolution permits DescribableResolution, PackageResolution, MultiClassResolution, MultiMemberResolution, ThrowingResolution, UnknownResolution {
	/**
	 * @return {@code true} if this resolution has failed.
	 */
	default boolean isUnknown() {
		return false;
	}
}
