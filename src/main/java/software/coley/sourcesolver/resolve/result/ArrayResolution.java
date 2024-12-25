package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ArrayEntry;

import javax.annotation.Nonnull;

non-sealed public interface ArrayResolution extends DescribableResolution {
	@Nonnull
	DescribableResolution getElementTypeResolution();

	@Nonnull
	@Override
	ArrayEntry getDescribableEntry();
}
