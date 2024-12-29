package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;

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

	@Override
	default boolean matches(@Nonnull Resolution other) {
		return other instanceof DescribableResolution otherDescribable &&
				getDescribableEntry().getDescriptor().equals(otherDescribable.getDescribableEntry().getDescriptor());
	}
}
