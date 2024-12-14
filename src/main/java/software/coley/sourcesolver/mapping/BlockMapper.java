package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BlockTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractStatementModel;
import software.coley.sourcesolver.model.BlockStatementModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class BlockMapper implements Mapper<BlockStatementModel, BlockTree> {
	@Nonnull
	@Override
	public BlockStatementModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull BlockTree tree) {
		Range range = extractRange(table, tree);
		List<AbstractStatementModel> statements = tree.getStatements().stream()
				.map(s -> context.map(StatementMapper.class, tree))
				.toList();
		return new BlockStatementModel(range, statements);

	}
}
