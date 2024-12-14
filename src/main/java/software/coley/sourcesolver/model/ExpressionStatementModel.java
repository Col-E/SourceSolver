package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class ExpressionStatementModel extends AbstractStatementModel {
	private final AbstractExpressionModel expression;

	public ExpressionStatementModel(@Nonnull Range range, @Nonnull AbstractExpressionModel expression) {
		super(range, expression);
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
		if (!super.equals(o)) return false;

		ExpressionStatementModel that = (ExpressionStatementModel) o;

		return expression.equals(that.expression);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + expression.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return expression.toString();
	}
}
