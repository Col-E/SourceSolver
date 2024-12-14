package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class UnknownStatementModel extends AbstractStatementModel {
	public UnknownStatementModel(@Nonnull Range range) {
		super(range);
	}

	@Override
	public String toString() {
		return "/* unknown */";
	}
}
