package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class CastExpressionModel extends AbstractExpressionModel {
	private final AbstractExpressionModel expression;
	private final Model type;

	public CastExpressionModel(@Nonnull Range range,
	                           @Nonnull Model type,
	                           @Nonnull AbstractExpressionModel expression) {
		super(range, type, expression);
		this.expression = expression;
		this.type = type;
	}

	@Nonnull
	public AbstractExpressionModel getExpression() {
		return expression;
	}

	@Nonnull
	public Model getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CastExpressionModel that = (CastExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!expression.equals(that.expression)) return false;
		return type.equals(that.type);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + expression.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "(" + type + ") " + expression;
	}
}
