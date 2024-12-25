package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.PrimitiveEntry;

import javax.annotation.Nonnull;

non-sealed public interface PrimitiveResolution extends DescribableResolution {
	@Nonnull
	@Override
	PrimitiveEntry getDescribableEntry();
}
