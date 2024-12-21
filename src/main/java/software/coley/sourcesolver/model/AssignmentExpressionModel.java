package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class AssignmentExpressionModel extends AbstractExpressionModel {
	private final AbstractExpressionModel variable;
	private final AbstractExpressionModel expression;
	private final Operator operator;

	public AssignmentExpressionModel(@Nonnull Range range,
	                                 @Nonnull AbstractExpressionModel variable,
	                                 @Nonnull AbstractExpressionModel expression,
	                                 @Nonnull Operator operator) {
		super(range, of(variable), of(expression));
		this.variable = variable;
		this.expression = expression;
		this.operator = operator;
	}

	@Nonnull
	public AbstractExpressionModel getVariable() {
		return variable;
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

		AssignmentExpressionModel that = (AssignmentExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!variable.equals(that.variable)) return false;
		if (!expression.equals(that.expression)) return false;
		return operator.equals(that.operator);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + variable.hashCode();
		result = 31 * result + expression.hashCode();
		result = 31 * result + operator.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return variable + " " + operator + " " + expression;
	}

	public enum Operator {
		SET("="),
		PLUS("+="),
		MINUS("-="),
		MULTIPLY("*="),
		DIVIDE("/="),
		REMAINDER("%="),
		BIT_OR("|="),
		BIT_AND("&="),
		BIT_XOR("^="),
		SHIFT_LEFT("<<="),
		SHIFT_RIGHT(">>="),
		SHIFT_RIGHT_UNSIGNED(">>>="),
		UNKNOWN("<asignop>");

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
