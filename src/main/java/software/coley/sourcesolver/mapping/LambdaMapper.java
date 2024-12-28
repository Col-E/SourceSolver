package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.LambdaExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.UnknownStatementModel;
import software.coley.sourcesolver.model.VariableModel;
import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class LambdaMapper implements Mapper<LambdaExpressionModel, LambdaExpressionTree> {
	@Nonnull
	@Override
	public LambdaExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull LambdaExpressionTree tree) {
		Range range = extractRange(table, tree);
		List<VariableModel> parameters = tree.getParameters().stream().map(p -> context.map(VariableMapper.class, p)).toList();
		Model body;
		LambdaExpressionModel.BodyKind kind;
		if (tree.getBodyKind() == LambdaExpressionTree.BodyKind.STATEMENT
				&& tree.getBody() instanceof StatementTree statementBody) {
			kind = LambdaExpressionModel.BodyKind.STATEMENT;
			body = context.map(StatementMapper.class, statementBody);
		} else if (tree.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION
				&& tree.getBody() instanceof ExpressionTree expressionTree) {
			kind = LambdaExpressionModel.BodyKind.EXPRESSION;
			body = context.map(ExpressionMapper.class, expressionTree);
		} else {
			kind = LambdaExpressionModel.BodyKind.STATEMENT;
			body = new UnknownStatementModel(extractRange(table, tree.getBody()), tree.getBody().toString());
		}
		return new LambdaExpressionModel(range, parameters, body, kind);
	}
}
