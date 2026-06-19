package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TryTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.BlockStatementModel;
import software.coley.sourcesolver.model.CatchModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.TryStatementModel;

import java.util.List;

public class TryMapper implements Mapper<TryStatementModel, TryTree> {
	@Nonnull
	@Override
	public TryStatementModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull TryTree tree) {
		BlockStatementModel block = context.map(BlockMapper.class, tree.getBlock());
		BlockStatementModel finallyBlock = tree.getFinallyBlock() == null ? null : context.map(BlockMapper.class, tree.getFinallyBlock());
		List<Model> resources = tree.getResources().stream().map(t -> {
			Model model;
			if (t instanceof ExpressionTree e)
				model = context.map(ExpressionMapper.class, e);
			else if (t instanceof StatementTree s)
				model = context.map(StatementMapper.class, s);
			else
				throw new IllegalStateException("Unsupported catch resource AST node: " + t.getClass().getSimpleName());
			return model;
		}).toList();
		List<CatchModel> catches = tree.getCatches().stream().map(c -> context.map(CatchMapper.class, c)).toList();
		return new TryStatementModel(extractor.get(tree), block, finallyBlock, resources, catches);
	}
}
