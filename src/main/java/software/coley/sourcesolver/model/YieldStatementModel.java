package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class YieldStatementModel extends AbstractStatementModel {
	private final AbstractExpressionModel expression;

	public YieldStatementModel(@Nonnull Range range, @Nonnull AbstractExpressionModel expression) {
		super(range);
		this.expression = expression;
	}

	@Nonnull
	public AbstractExpressionModel getExpression() {
		return expression;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		YieldStatementModel that = (YieldStatementModel) o;

		return expression.equals(that.expression) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + expression.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "yield " + expression;
	}
}
