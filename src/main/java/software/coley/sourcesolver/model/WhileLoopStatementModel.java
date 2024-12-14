package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class WhileLoopStatementModel extends AbstractStatementModel {
	private final AbstractExpressionModel condition;
	private final AbstractStatementModel statement;

	public WhileLoopStatementModel(@Nonnull Range range, @Nonnull AbstractExpressionModel condition, @Nonnull AbstractStatementModel statement) {
		super(range, condition, statement);
		this.condition = condition;
		this.statement = statement;
	}

	@Nonnull
	public AbstractExpressionModel getCondition() {
		return condition;
	}

	@Nonnull
	public AbstractStatementModel getStatement() {
		return statement;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		WhileLoopStatementModel that = (WhileLoopStatementModel) o;

		if (!condition.equals(that.condition)) return false;
		return statement.equals(that.statement);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + condition.hashCode();
		result = 31 * result + statement.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "while (" + condition + ") { " + statement + " }";
	}
}
