package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.NullEntry;

import javax.annotation.Nonnull;

non-sealed public interface NullResolution extends DescribableResolution {
	@Nonnull
	@Override
	default NullEntry getDescribableEntry() {
		return NullEntry.INSTANCE;
	}
}
