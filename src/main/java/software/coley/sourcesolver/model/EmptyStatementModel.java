package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class EmptyStatementModel extends AbstractStatementModel {
	public EmptyStatementModel(@Nonnull Range range) {
		super(range);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o.getClass() != EmptyStatementModel.class) return false;
		return getRange() == ((EmptyStatementModel) o).getRange();
	}

	@Override
	public int hashCode() {
		return getRange().hashCode();
	}

	@Override
	public String toString() {
		return "{}";
	}
}
