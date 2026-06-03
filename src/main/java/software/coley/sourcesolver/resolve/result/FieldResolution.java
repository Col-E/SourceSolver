package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.resolve.entry.FieldEntry;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;

/**
 * Resolution of a field.
 *
 * @author Matt Coley
 */
non-sealed public interface FieldResolution extends MemberResolution {
	/**
	 * @return The resolved field.
	 */
	@Nonnull
	FieldEntry getFieldEntry();

	/**
	 * @return Resolved value type of the field after applying receiver generic arguments.
	 */
	@Nonnull
	DescribableEntry getResolvedFieldType();

	@Nonnull
	@Override
	default FieldEntry getDescribableEntry() {
		return getFieldEntry();
	}
}
