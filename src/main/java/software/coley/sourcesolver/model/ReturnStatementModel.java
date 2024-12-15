package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class ReturnStatementModel extends AbstractStatementModel {
	private final AbstractExpressionModel expression;

	public ReturnStatementModel(@Nonnull Range range, @Nullable AbstractExpressionModel expression) {
		super(range, of(expression));
		this.expression = expression;
	}

	@Nullable
	public AbstractExpressionModel getExpression() {
		return expression;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ReturnStatementModel that = (ReturnStatementModel) o;

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
		if (expression == null)
			return "return;";
		return "return " + expression + ";";
	}
}
