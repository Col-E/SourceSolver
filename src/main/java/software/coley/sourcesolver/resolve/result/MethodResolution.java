package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.MethodEntry;

import javax.annotation.Nonnull;

non-sealed public interface MethodResolution extends MemberResolution {
	@Nonnull
	MethodEntry getMethodEntry();

	@Nonnull
	@Override
	default DescribableEntry getDescribableEntry() {
		return getMethodEntry();
	}
}
