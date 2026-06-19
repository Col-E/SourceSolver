package software.coley.sourcesolver.mapping;

import com.sun.source.tree.Tree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.NameExpressionModel;

public class NameMapper implements Mapper<NameExpressionModel, Tree> {
	@Nonnull
	@Override
	public NameExpressionModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull Tree tree) {
		return new NameExpressionModel(extractor.get(tree), tree.toString());
	}
}
