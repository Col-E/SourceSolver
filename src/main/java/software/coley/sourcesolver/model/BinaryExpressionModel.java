package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class BinaryExpressionModel extends AbstractExpressionModel {
	private final AbstractExpressionModel left;
	private final AbstractExpressionModel right;
	private final Operator operator;

	public BinaryExpressionModel(@Nonnull Range range,
	                             @Nonnull AbstractExpressionModel left,
	                             @Nonnull AbstractExpressionModel right,
	                             @Nonnull Operator operator) {
		super(range, left, right);
		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	@Nonnull
	public AbstractExpressionModel getLeft() {
		return left;
	}

	@Nonnull
	public AbstractExpressionModel getRight() {
		return right;
	}

	@Nonnull
	public Operator getOperator() {
		return operator;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BinaryExpressionModel that = (BinaryExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!left.equals(that.left)) return false;
		if (!right.equals(that.right)) return false;
		return operator == that.operator;
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + left.hashCode();
		result = 31 * result + right.hashCode();
		result = 31 * result + operator.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return left + " " + operator + " " + right;
	}

	public enum Operator {
		PLUS("+"),
		MINUS("-"),
		MULTIPLY("*"),
		DIVIDE("/"),
		REMAINDER("%"),
		EQUALS("=="),
		NOT_EQUALS("!="),
		BIT_OR("|"),
		BIT_AND("&"),
		BIT_XOR("^"),
		CONDITIONAL_OR("||"),
		CONDITIONAL_AND("&&"),
		SHIFT_LEFT("<<"),
		SHIFT_RIGHT(">>"),
		SHIFT_RIGHT_UNSIGNED(">>>"),
		RELATION_LESS("<"),
		RELATION_GREATER(">"),
		RELATION_LESS_EQUAL("<="),
		RELATION_GREATER_EQUAL(">="),
		RELATION_INSTANCEOF("instanceof"),
		UNKNOWN("<binop>");

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
