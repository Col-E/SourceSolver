package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ClassEntry;

import javax.annotation.Nonnull;

sealed public interface MemberResolution extends DescribableResolution permits FieldResolution, MethodResolution {
	@Nonnull
	ClassEntry getOwnerEntry();
}
