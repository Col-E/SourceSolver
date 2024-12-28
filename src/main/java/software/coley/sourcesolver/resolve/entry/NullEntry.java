package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;

/**
 * Metadata model for a null constant.
 *
 * @author Matt Coley
 */
public non-sealed interface NullEntry extends DescribableEntry {
	NullEntry INSTANCE = new BasicNullEntry();

	@Nonnull
	@Override
	default String getDescriptor() {
		return "Ljava/lang/Object;";
	}

	@Nonnull
	@Override
	default ArrayEntry toArrayEntry(int dimensions) {
		return DescribableEntry.super.toArrayEntry(dimensions);
	}

	@Override
	default boolean isAssignableFrom(@Nonnull DescribableEntry other) {
		return true;
	}
}
