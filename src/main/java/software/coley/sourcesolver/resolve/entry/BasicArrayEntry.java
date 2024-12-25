package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

public record BasicArrayEntry(int dimensions, @Nonnull DescribableEntry elementEntry) implements ArrayEntry {
	@Override
	public int getDimensions() {
		return dimensions;
	}

	@Nonnull
	@Override
	public DescribableEntry getElementEntry() {
		return elementEntry;
	}

	@Nonnull
	@Override
	public String getDescriptor() {
		return "[".repeat(dimensions) + elementEntry.getDescriptor();
	}
}
