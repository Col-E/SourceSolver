package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.FieldEntry;

import javax.annotation.Nonnull;

non-sealed public interface FieldResolution extends MemberResolution {
	@Nonnull
	FieldEntry getFieldEntry();

	@Nonnull
	@Override
	default DescribableEntry getDescribableEntry() {
		return getFieldEntry();
	}
}
