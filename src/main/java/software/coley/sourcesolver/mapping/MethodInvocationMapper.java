package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.MethodInvocationExpressionModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MethodInvocationMapper implements Mapper<MethodInvocationExpressionModel, MethodInvocationTree> {
	@Nonnull
	@Override
	public MethodInvocationExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull MethodInvocationTree tree) {
		Range range = extractRange(table, tree);
		List<AbstractModel> typeArguments = tree.getTypeArguments() == null ? Collections.emptyList() :
				tree.getTypeArguments().stream().map(t -> {
					if (t instanceof ExpressionTree e)
						return context.map(ExpressionMapper.class, e);
					else if (t instanceof StatementTree s)
						return context.map(StatementMapper.class, s);
					else
						throw new IllegalStateException("Unsupported type argument AST node: " + t.getClass().getSimpleName());
				}).toList();
		AbstractExpressionModel methodSelect = context.map(ExpressionMapper.class, tree.getMethodSelect());
		List<AbstractExpressionModel> arguments = tree.getArguments().stream().map(t -> context.map(ExpressionMapper.class, t)).toList();
		return new MethodInvocationExpressionModel(range, typeArguments, methodSelect, arguments);
	}
}
