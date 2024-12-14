package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class EmptyStatementModel extends AbstractStatementModel {
	public EmptyStatementModel(@Nonnull Range range) {
		super(range);
	}

	@Override
	public String toString() {
		return "{}";
	}
}
