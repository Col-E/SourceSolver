package software.coley.sourcesolver.mapping;

import com.sun.source.tree.UnaryTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.UnaryExpressionModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class UnaryMapper implements Mapper<UnaryExpressionModel, UnaryTree> {
	@Nonnull
	@Override
	public UnaryExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull UnaryTree tree) {
		Range range = extractRange(table, tree);
		AbstractExpressionModel expression = context.map(ExpressionMapper.class, tree.getExpression());
		UnaryExpressionModel.Operator operator = switch (tree.getKind()) {
			case POSTFIX_INCREMENT -> UnaryExpressionModel.Operator.POST_INCREMENT;
			case POSTFIX_DECREMENT -> UnaryExpressionModel.Operator.POST_DECREMENT;
			case PREFIX_INCREMENT -> UnaryExpressionModel.Operator.PRE_INCREMENT;
			case PREFIX_DECREMENT -> UnaryExpressionModel.Operator.PRE_DECREMENT;
			case UNARY_PLUS -> UnaryExpressionModel.Operator.POSITIVE;
			case UNARY_MINUS -> UnaryExpressionModel.Operator.NEGATIVE;
			case LOGICAL_COMPLEMENT -> UnaryExpressionModel.Operator.NOT;
			case BITWISE_COMPLEMENT -> UnaryExpressionModel.Operator.BIT_NOT;
			default -> UnaryExpressionModel.Operator.UNKNOWN;
		};
		return new UnaryExpressionModel(range, expression, operator);
	}
}
