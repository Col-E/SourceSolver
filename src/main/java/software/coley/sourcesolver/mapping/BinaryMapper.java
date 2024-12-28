package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BinaryTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.BinaryExpressionModel;
import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class BinaryMapper implements Mapper<BinaryExpressionModel, BinaryTree> {
	@Nonnull
	@Override
	public BinaryExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull BinaryTree tree) {
		Range range = extractRange(table, tree);
		AbstractExpressionModel left = context.map(ExpressionMapper.class, tree.getLeftOperand());
		AbstractExpressionModel right = context.map(ExpressionMapper.class, tree.getRightOperand());
		BinaryExpressionModel.Operator operator =
				switch (tree.getKind()) {
					case PLUS -> BinaryExpressionModel.Operator.PLUS;
					case MINUS -> BinaryExpressionModel.Operator.MINUS;
					case MULTIPLY -> BinaryExpressionModel.Operator.MULTIPLY;
					case DIVIDE -> BinaryExpressionModel.Operator.DIVIDE;
					case REMAINDER -> BinaryExpressionModel.Operator.REMAINDER;
					case EQUAL_TO -> BinaryExpressionModel.Operator.EQUALS;
					case NOT_EQUAL_TO -> BinaryExpressionModel.Operator.NOT_EQUALS;
					case OR -> BinaryExpressionModel.Operator.BIT_OR;
					case AND -> BinaryExpressionModel.Operator.BIT_AND;
					case XOR -> BinaryExpressionModel.Operator.BIT_XOR;
					case CONDITIONAL_OR -> BinaryExpressionModel.Operator.CONDITIONAL_OR;
					case CONDITIONAL_AND -> BinaryExpressionModel.Operator.CONDITIONAL_AND;
					case LEFT_SHIFT -> BinaryExpressionModel.Operator.SHIFT_LEFT;
					case RIGHT_SHIFT -> BinaryExpressionModel.Operator.SHIFT_RIGHT;
					case UNSIGNED_RIGHT_SHIFT -> BinaryExpressionModel.Operator.SHIFT_RIGHT_UNSIGNED;
					case LESS_THAN -> BinaryExpressionModel.Operator.RELATION_LESS;
					case LESS_THAN_EQUAL -> BinaryExpressionModel.Operator.RELATION_LESS_EQUAL;
					case GREATER_THAN -> BinaryExpressionModel.Operator.RELATION_GREATER;
					case GREATER_THAN_EQUAL -> BinaryExpressionModel.Operator.RELATION_GREATER_EQUAL;
					case INSTANCE_OF -> BinaryExpressionModel.Operator.RELATION_INSTANCEOF;
					default -> BinaryExpressionModel.Operator.UNKNOWN;
				};
		return new BinaryExpressionModel(range, left, right, operator);

	}
}
