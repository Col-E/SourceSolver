package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;

public class MethodBodyModel extends AbstractModel {
	private final List<AbstractStatementModel> statements;

	public MethodBodyModel(@Nonnull Range range, @Nonnull List<AbstractStatementModel> statements) {
		super(range, statements);
		this.statements = statements;
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

		MethodBodyModel that = (MethodBodyModel) o;

		return statements.equals(that.statements);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + statements.hashCode();
		return result;
	}
}
