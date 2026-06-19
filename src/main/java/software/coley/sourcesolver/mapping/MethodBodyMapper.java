package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BlockTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AbstractStatementModel;
import software.coley.sourcesolver.model.MethodBodyModel;

import java.util.List;

public class MethodBodyMapper implements Mapper<MethodBodyModel, BlockTree> {
	@Nonnull
	@Override
	public MethodBodyModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull BlockTree tree) {
		List<AbstractStatementModel> list = tree.getStatements().stream().map(s -> context.map(StatementMapper.class, s)).toList();
		return new MethodBodyModel(extractor.get(tree), list);
	}
}
