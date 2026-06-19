package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BlockTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AbstractStatementModel;
import software.coley.sourcesolver.model.BlockStatementModel;
import software.coley.sourcesolver.util.Range;

import java.util.List;

public class BlockMapper implements Mapper<BlockStatementModel, BlockTree> {
	@Nonnull
	@Override
	public BlockStatementModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull BlockTree tree) {
		Range range = extractor.get(tree);
		List<AbstractStatementModel> statements = tree.getStatements().stream()
				.map(s -> context.map(StatementMapper.class, s))
				.toList();
		return new BlockStatementModel(range, statements);

	}
}
