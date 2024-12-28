package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BlockTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractStatementModel;
import software.coley.sourcesolver.model.MethodBodyModel;

import jakarta.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MethodBodyMapper implements Mapper<MethodBodyModel, BlockTree> {
	@Nonnull
	@Override
	public MethodBodyModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull BlockTree tree) {
		List<AbstractStatementModel> list = tree.getStatements().stream().map(s -> context.map(StatementMapper.class, s)).toList();
		return new MethodBodyModel(extractRange(table, tree), list);
	}
}
