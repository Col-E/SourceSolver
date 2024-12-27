package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.DescribableEntry;

import javax.annotation.Nonnull;

sealed public interface DescribableResolution extends Resolution permits ArrayResolution, ClassResolution, MemberResolution, PrimitiveResolution, NullResolution {
	@Nonnull
	DescribableEntry getDescribableEntry();
}
