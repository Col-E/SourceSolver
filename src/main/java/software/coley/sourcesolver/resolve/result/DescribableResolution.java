package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.DescribableEntry;

import javax.annotation.Nonnull;

sealed public interface DescribableResolution extends Resolution permits MemberResolution, ClassResolution, PrimitiveResolution, ArrayResolution {
	@Nonnull
	DescribableEntry getDescribableEntry();
}
