package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class CaseModel extends AbstractModel {
	private final List<AbstractExpressionModel> expressions; // case <expression>
	private final List<AbstractStatementModel> statements; // body of cases

	public CaseModel(@Nonnull Range range, @Nonnull List<AbstractExpressionModel> expressions, @Nonnull List<AbstractStatementModel> statements) {
		super(range, of(expressions), of(statements));
		this.expressions = expressions;
		this.statements = statements;
	}

	@Nonnull
	public List<AbstractExpressionModel> getExpressions() {
		return expressions;
	}

	@Nonnull
	public List<AbstractStatementModel> getStatements() {
		return statements;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		CaseModel caseModel = (CaseModel) o;

		if (!expressions.equals(caseModel.expressions)) return false;
		return statements.equals(caseModel.statements);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + expressions.hashCode();
		result = 31 * result + statements.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "case " + expressions.stream().map(Object::toString).collect(Collectors.joining(", ")) + ":\n    "
				+ statements.stream().map(Object::toString).collect(Collectors.joining("\n    "));
	}
}
