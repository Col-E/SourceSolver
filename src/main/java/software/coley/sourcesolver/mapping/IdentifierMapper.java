package software.coley.sourcesolver.mapping;

import com.sun.source.tree.IdentifierTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.NameExpressionModel;

public class IdentifierMapper implements Mapper<NameExpressionModel, IdentifierTree> {
	@Nonnull
	@Override
	public NameExpressionModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull IdentifierTree tree) {
		return new NameExpressionModel(extractor.get(tree), tree.toString());
	}
}
