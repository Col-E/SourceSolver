package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class SynchronizedStatementModel extends AbstractStatementModel {
	private final AbstractExpressionModel expression;
	private final BlockStatementModel block;

	public SynchronizedStatementModel(@Nonnull Range range, @Nonnull AbstractExpressionModel expression, @Nonnull BlockStatementModel block) {
		super(range);
		this.expression = expression;
		this.block = block;
	}

	@Nonnull
	public AbstractExpressionModel getExpression() {
		return expression;
	}

	@Nonnull
	public BlockStatementModel getBlock() {
		return block;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SynchronizedStatementModel that = (SynchronizedStatementModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!expression.equals(that.expression)) return false;
		return block.equals(that.block);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + expression.hashCode();
		result = 31 * result + block.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "synchronized (" + expression + ") " + block;
	}
}
