package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class CatchModel extends AbstractModel {
	private final VariableModel parameter;
	private final BlockStatementModel block;

	public CatchModel(@Nonnull Range range, @Nonnull VariableModel parameter, @Nonnull BlockStatementModel block) {
		super(range, parameter, block);
		this.parameter = parameter;
		this.block = block;
	}

	@Nonnull
	public VariableModel getParameter() {
		return parameter;
	}

	@Nonnull
	public BlockStatementModel getBlock() {
		return block;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CatchModel that = (CatchModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!parameter.equals(that.parameter)) return false;
		return block.equals(that.block);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + parameter.hashCode();
		result = 31 * result + block.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "catch (" + parameter + ") " + block;
	}
}
