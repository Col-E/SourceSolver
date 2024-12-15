package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class UnaryExpressionModel extends AbstractExpressionModel {
	private final AbstractExpressionModel expression;
	private final Operator operator;

	public UnaryExpressionModel(@Nonnull Range range,
	                            @Nonnull AbstractExpressionModel expression,
	                            @Nonnull Operator operator) {
		super(range, expression);
		this.expression = expression;
		this.operator = operator;
	}

	@Nonnull
	public AbstractExpressionModel getExpression() {
		return expression;
	}

	@Nonnull
	public Operator getOperator() {
		return operator;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UnaryExpressionModel that = (UnaryExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!expression.equals(that.expression)) return false;
		return operator == that.operator;
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + expression.hashCode();
		result = 31 * result + operator.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return operator + " " + expression;
	}

	public enum Operator {
		POST_INCREMENT("++"),
		POST_DECREMENT("--"),
		PRE_INCREMENT("++"),
		PRE_DECREMENT("--"),
		POSITIVE("+"),
		NEGATIVE("-"),
		NOT("!"),
		BIT_NOT("~"),
		UNKNOWN("<unop>");

		private final String text;

		Operator(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}
}
