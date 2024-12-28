package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class SwitchStatementModel extends AbstractStatementModel {
	private final AbstractExpressionModel expression;
	private final List<CaseModel> cases;

	public SwitchStatementModel(@Nonnull Range range, @Nonnull AbstractExpressionModel expression, @Nonnull List<CaseModel> cases) {
		super(range, of(expression), of(cases));

		this.expression = expression;
		this.cases = cases;
	}

	@Nonnull
	public AbstractExpressionModel getExpression() {
		return expression;
	}

	@Nonnull
	public List<CaseModel> getCases() {
		return cases;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SwitchStatementModel that = (SwitchStatementModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!expression.equals(that.expression)) return false;
		return cases.equals(that.cases);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + expression.hashCode();
		result = 31 * result + cases.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "switch " + expression + ") {\n" +
				cases.stream().map(CaseModel::toString).collect(Collectors.joining("\n")) +
				"\n}";
	}
}
