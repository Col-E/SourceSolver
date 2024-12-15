package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class ForLoopStatementModel extends AbstractStatementModel {
	private final List<AbstractStatementModel> initializerStatements;
	private final List<AbstractStatementModel> updateStatements;
	private final AbstractExpressionModel condition;
	private final AbstractStatementModel statement;

	public ForLoopStatementModel(@Nonnull Range range,
	                             @Nonnull List<AbstractStatementModel> initializerStatements,
	                             @Nonnull List<AbstractStatementModel> updateStatements,
	                             @Nonnull AbstractExpressionModel condition,
	                             @Nonnull AbstractStatementModel statement) {
		super(range, condition, statement);
		this.initializerStatements = initializerStatements;
		this.updateStatements = updateStatements;
		this.condition = condition;
		this.statement = statement;
	}

	@Nonnull
	public List<AbstractStatementModel> getInitializerStatements() {
		return initializerStatements;
	}

	@Nonnull
	public List<AbstractStatementModel> getUpdateStatements() {
		return updateStatements;
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

		ForLoopStatementModel that = (ForLoopStatementModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!initializerStatements.equals(that.initializerStatements)) return false;
		if (!updateStatements.equals(that.updateStatements)) return false;
		if (!condition.equals(that.condition)) return false;
		return statement.equals(that.statement);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + initializerStatements.hashCode();
		result = 31 * result + updateStatements.hashCode();
		result = 31 * result + condition.hashCode();
		result = 31 * result + statement.hashCode();
		return result;
	}

	@Override
	public String toString() {
		String initStr = initializerStatements.stream().map(Object::toString).collect(Collectors.joining("; "));
		String updateStr = updateStatements.stream().map(Object::toString).collect(Collectors.joining("; "));
		return "for (" + initStr + "; " + condition + "; " + updateStr + ") { " + statement + " }";
	}

}
