package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class EnhancedForLoopStatementModel extends AbstractStatementModel {
	private final VariableModel variable;
	private final AbstractExpressionModel expression;
	private final AbstractStatementModel statement;

	public EnhancedForLoopStatementModel(@Nonnull Range range, @Nonnull VariableModel variable,
	                                     @Nonnull AbstractExpressionModel expression, @Nonnull AbstractStatementModel statement) {
		super(range, variable, expression, statement);
		this.variable = variable;
		this.expression = expression;
		this.statement = statement;
	}

	@Nonnull
	public VariableModel getVariable() {
		return variable;
	}

	@Nonnull
	public AbstractExpressionModel getExpression() {
		return expression;
	}

	@Nonnull
	public AbstractStatementModel getStatement() {
		return statement;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EnhancedForLoopStatementModel that = (EnhancedForLoopStatementModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!variable.equals(that.variable)) return false;
		if (!expression.equals(that.expression)) return false;
		return statement.equals(that.statement);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + variable.hashCode();
		result = 31 * result + expression.hashCode();
		result = 31 * result + statement.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "for (" + variable + " : " + expression + ") { " + statement + " }";
	}
}
