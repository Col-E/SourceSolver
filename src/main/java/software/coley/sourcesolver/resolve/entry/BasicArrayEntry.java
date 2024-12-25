package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

public class BasicArrayEntry implements ArrayEntry {
	private final int dimensions;
	private final DescribableEntry elementEntry;

	public BasicArrayEntry(int dimensions, @Nonnull DescribableEntry elementEntry) {
		this.dimensions = dimensions;
		this.elementEntry = elementEntry;
	}

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BasicArrayEntry that = (BasicArrayEntry) o;

		if (dimensions != that.dimensions) return false;
		return elementEntry.equals(that.elementEntry);
	}

	@Override
	public int hashCode() {
		int result = dimensions;
		result = 31 * result + elementEntry.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return getDescriptor();
	}
}
