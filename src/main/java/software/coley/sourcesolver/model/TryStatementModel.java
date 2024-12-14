package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class TryStatementModel extends AbstractStatementModel {
	private final BlockStatementModel block;
	private final BlockStatementModel finallyBlock;
	private final List<AbstractModel> resources;
	private final List<CatchModel> catches;

	public TryStatementModel(@Nonnull Range range, @Nonnull BlockStatementModel block,
	                         @Nullable BlockStatementModel finallyBlock, @Nonnull List<AbstractModel> resources,
	                         @Nonnull List<CatchModel> catches) {
		super(range, of(block), of(finallyBlock), of(resources), of(catches));
		this.block = block;
		this.finallyBlock = finallyBlock;
		this.resources = resources;
		this.catches = catches;
	}

	@Nonnull
	public BlockStatementModel getBlock() {
		return block;
	}

	@Nullable
	public BlockStatementModel getFinallyBlock() {
		return finallyBlock;
	}

	@Nonnull
	public List<AbstractModel> getResources() {
		return resources;
	}

	@Nonnull
	public List<CatchModel> getCatches() {
		return catches;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		TryStatementModel that = (TryStatementModel) o;

		if (!block.equals(that.block)) return false;
		if (!Objects.equals(finallyBlock, that.finallyBlock)) return false;
		if (!resources.equals(that.resources)) return false;
		return catches.equals(that.catches);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + block.hashCode();
		result = 31 * result + (finallyBlock != null ? finallyBlock.hashCode() : 0);
		result = 31 * result + resources.hashCode();
		result = 31 * result + catches.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (resources.isEmpty())
			sb.append("try ");
		else
			sb.append("try (").append(resources.stream().map(Object::toString).collect(Collectors.joining("; "))).append(")");
		sb.append(block)
				.append('\n')
				.append(catches.stream().map(CatchModel::toString).collect(Collectors.joining("\n")));
		if (finallyBlock != null)
			sb.append(finallyBlock);
		return sb.toString();
	}
}
