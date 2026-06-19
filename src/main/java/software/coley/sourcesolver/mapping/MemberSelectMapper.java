package software.coley.sourcesolver.mapping;

import com.sun.source.tree.MemberSelectTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.MemberSelectExpressionModel;
import software.coley.sourcesolver.model.Model;

public class MemberSelectMapper implements Mapper<MemberSelectExpressionModel, MemberSelectTree> {
	@Nonnull
	@Override
	public MemberSelectExpressionModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull MemberSelectTree tree) {
		String name = tree.getIdentifier().toString();
		Model selectContext = context.map(ExpressionMapper.class, tree.getExpression());
		return new MemberSelectExpressionModel(extractor.get(tree), name, selectContext);
	}
}
