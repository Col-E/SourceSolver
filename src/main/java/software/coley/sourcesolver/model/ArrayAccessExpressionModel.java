package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class ArrayAccessExpressionModel extends AbstractExpressionModel {
	private final AbstractExpressionModel expression;
	private final AbstractExpressionModel index;

	public ArrayAccessExpressionModel(@Nonnull Range range,
	                                  @Nonnull AbstractExpressionModel expression,
	                                  @Nonnull AbstractExpressionModel index) {
		super(range, expression, index);
		this.expression = expression;
		this.index = index;
	}

	@Nonnull
	public AbstractExpressionModel getExpression() {
		return expression;
	}

	@Nonnull
	public AbstractExpressionModel getIndex() {
		return index;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ArrayAccessExpressionModel that = (ArrayAccessExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!expression.equals(that.expression)) return false;
		return index.equals(that.index);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + expression.hashCode();
		result = 31 * result + index.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return expression + "[" + index + "]";
	}
}
