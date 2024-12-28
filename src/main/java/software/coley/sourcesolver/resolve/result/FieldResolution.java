package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.FieldEntry;

import javax.annotation.Nonnull;

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

	@Nonnull
	@Override
	default FieldEntry getDescribableEntry() {
		return getFieldEntry();
	}
}
