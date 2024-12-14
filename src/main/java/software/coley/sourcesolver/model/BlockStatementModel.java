package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class BlockStatementModel extends AbstractStatementModel {
	private final List<AbstractStatementModel> statements;

	public BlockStatementModel(@Nonnull Range range, @Nonnull List<AbstractStatementModel> statements) {
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

		BlockStatementModel that = (BlockStatementModel) o;

		return statements.equals(that.statements);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + statements.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "{\n    " + statements.stream().map(Object::toString).collect(Collectors.joining("    \n")) + "\n}";
	}
}
