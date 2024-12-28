package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class NameExpressionModel extends AbstractExpressionModel implements NamedModel {
	private final String name;

	public NameExpressionModel(@Nonnull Range range, @Nonnull String name) {
		super(range);
		this.name = name;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NameExpressionModel that = (NameExpressionModel) o;

		return name.equals(that.name) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return name;
	}
}
