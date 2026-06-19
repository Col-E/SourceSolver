package software.coley.sourcesolver.mapping;

import com.sun.source.tree.LiteralTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.LiteralExpressionModel;

public class LiteralMapper implements Mapper<LiteralExpressionModel, LiteralTree> {
	@Nonnull
	@Override
	public LiteralExpressionModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull LiteralTree tree) {
		Object content = tree.getValue();
		var kind = switch (tree.getKind()) {
			case INT_LITERAL -> LiteralExpressionModel.Kind.INT;
			case LONG_LITERAL -> LiteralExpressionModel.Kind.LONG;
			case FLOAT_LITERAL -> LiteralExpressionModel.Kind.FLOAT;
			case DOUBLE_LITERAL -> LiteralExpressionModel.Kind.DOUBLE;
			case BOOLEAN_LITERAL -> LiteralExpressionModel.Kind.BOOLEAN;
			case CHAR_LITERAL -> LiteralExpressionModel.Kind.CHAR;
			case STRING_LITERAL -> LiteralExpressionModel.Kind.STRING;
			case NULL_LITERAL -> LiteralExpressionModel.Kind.NULL;
			default -> LiteralExpressionModel.Kind.ERROR;
		};
		return new LiteralExpressionModel(extractor.get(tree), kind, content);
	}
}
