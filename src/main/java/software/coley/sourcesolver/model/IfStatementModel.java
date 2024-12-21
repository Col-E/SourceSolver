package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class IfStatementModel extends AbstractStatementModel {
	private final AbstractExpressionModel condition;
	private final AbstractStatementModel thenCaseStatement;
	private final AbstractStatementModel elseCaseStatement;

	public IfStatementModel(@Nonnull Range range,
	                        @Nonnull AbstractExpressionModel condition,
	                        @Nonnull AbstractStatementModel thenCaseStatement,
	                        @Nullable AbstractStatementModel elseCaseStatement) {
		super(range, of(condition), of(thenCaseStatement), of(elseCaseStatement));
		this.condition = condition;
		this.thenCaseStatement = thenCaseStatement;
		this.elseCaseStatement = elseCaseStatement;
	}

	@Nonnull
	public AbstractExpressionModel getCondition() {
		return condition;
	}

	@Nonnull
	public AbstractStatementModel getThenCaseStatement() {
		return thenCaseStatement;
	}

	@Nullable
	public AbstractStatementModel getElseCaseStatement() {
		return elseCaseStatement;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IfStatementModel that = (IfStatementModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!condition.equals(that.condition)) return false;
		if (!thenCaseStatement.equals(that.thenCaseStatement)) return false;
		return Objects.equals(elseCaseStatement, that.elseCaseStatement);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + condition.hashCode();
		result = 31 * result + thenCaseStatement.hashCode();
		result = 31 * result + (elseCaseStatement != null ? elseCaseStatement.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("if (").append(condition.toString()).append(") { ")
				.append(thenCaseStatement.toString()).append(" }");
		if (elseCaseStatement != null)
			sb.append(" else ").append(elseCaseStatement);
		return sb.toString();
	}
}
