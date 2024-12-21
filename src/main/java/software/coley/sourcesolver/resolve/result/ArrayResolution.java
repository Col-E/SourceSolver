package software.coley.sourcesolver.resolve.result;

import javax.annotation.Nonnull;

non-sealed public interface ArrayResolution extends DescribableResolution {
	@Nonnull
	DescribableResolution getElementTypeResolution();
}
