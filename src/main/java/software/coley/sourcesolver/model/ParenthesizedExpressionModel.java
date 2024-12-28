package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class ParenthesizedExpressionModel extends AbstractExpressionModel {
	private final AbstractExpressionModel expression;

	public ParenthesizedExpressionModel(@Nonnull Range range,
	                                    @Nonnull AbstractExpressionModel expression) {
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

		ParenthesizedExpressionModel that = (ParenthesizedExpressionModel) o;

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
		return "(" + expression + ")";
	}
}
