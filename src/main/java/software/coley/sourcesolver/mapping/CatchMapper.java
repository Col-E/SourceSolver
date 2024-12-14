package software.coley.sourcesolver.mapping;

import com.sun.source.tree.CatchTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.BlockStatementModel;
import software.coley.sourcesolver.model.CatchModel;
import software.coley.sourcesolver.model.VariableModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class CatchMapper implements Mapper<CatchModel, CatchTree> {
	@Nonnull
	@Override
	public CatchModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull CatchTree tree) {
		Range range = extractRange(table, tree);
		VariableModel parameter = context.map(VariableMapper.class, tree.getParameter());
		BlockStatementModel block = context.map(BlockMapper.class, tree.getBlock());
		return new CatchModel(range, parameter, block);

	}
}
