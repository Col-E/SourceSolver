package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;

import javax.annotation.Nonnull;

non-sealed public interface ClassResolution extends DescribableResolution {
	@Nonnull
	ClassEntry getClassEntry();

	@Nonnull
	@Override
	default DescribableEntry getDescribableEntry() {
		return getClassEntry();
	}
}
