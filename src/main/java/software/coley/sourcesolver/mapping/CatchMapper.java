package software.coley.sourcesolver.mapping;

import com.sun.source.tree.CatchTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.BlockStatementModel;
import software.coley.sourcesolver.model.CatchModel;
import software.coley.sourcesolver.model.VariableModel;
import software.coley.sourcesolver.util.Range;

public class CatchMapper implements Mapper<CatchModel, CatchTree> {
	@Nonnull
	@Override
	public CatchModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull CatchTree tree) {
		Range range = extractor.get(tree);
		VariableModel parameter = context.map(VariableMapper.class, tree.getParameter());
		BlockStatementModel block = context.map(BlockMapper.class, tree.getBlock());
		return new CatchModel(range, parameter, block);

	}
}
