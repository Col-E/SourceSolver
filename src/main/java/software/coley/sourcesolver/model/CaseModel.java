package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class CaseModel extends AbstractModel {
	private final List<AbstractExpressionModel> expressions; // case <expression>
	private final List<AbstractStatementModel> statements; // body of cases
	private final Model body; // body of cases

	public CaseModel(@Nonnull Range range, @Nonnull List<AbstractExpressionModel> expressions,
	                 @Nonnull List<AbstractStatementModel> statements, @Nullable Model body) {
		super(range, of(expressions), of(statements), of(body));
		this.expressions = expressions;
		this.statements = statements;
		this.body = body;
	}

	@Nonnull
	public List<AbstractExpressionModel> getExpressions() {
		return expressions;
	}

	@Nonnull
	public List<AbstractStatementModel> getStatements() {
		return statements;
	}

	@Nullable
	public Model getBody() {
		return body;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CaseModel caseModel = (CaseModel) o;

		if (!expressions.equals(caseModel.expressions)) return false;
		if (!statements.equals(caseModel.statements)) return false;
		return Objects.equals(body, caseModel.body);
	}

	@Override
	public int hashCode() {
		int result = expressions.hashCode();
		result = 31 * result + statements.hashCode();
		result = 31 * result + (body != null ? body.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		String expressions = this.expressions.stream().map(Object::toString).collect(Collectors.joining(", "));
		if (body != null)
			return "case " + expressions + " -> " + body;
		return "case " + expressions + ":\n    "
				+ statements.stream().map(Object::toString).collect(Collectors.joining("\n    "));
	}
}
