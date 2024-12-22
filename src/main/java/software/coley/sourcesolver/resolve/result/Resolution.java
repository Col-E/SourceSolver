package software.coley.sourcesolver.resolve.result;

public sealed interface Resolution permits DescribableResolution, PackageResolution, MultiClassResolution, MultiMemberResolution, UnknownResolution {
	default boolean isUnknown() {
		return false;
	}
}
