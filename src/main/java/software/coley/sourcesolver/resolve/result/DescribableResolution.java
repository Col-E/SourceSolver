package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.DescribableEntry;

import javax.annotation.Nonnull;

/**
 * Resolution of some content that can be represented with a descriptor.
 *
 * @author Matt Coley
 */
sealed public interface DescribableResolution extends Resolution permits ArrayResolution, ClassResolution, MemberResolution, PrimitiveResolution, NullResolution {
	/**
	 * @return The resolved describable entry.
	 */
	@Nonnull
	DescribableEntry getDescribableEntry();
}
