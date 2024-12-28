package software.coley.sourcesolver.mapping;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.MethodInvocationExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MethodInvocationMapper implements Mapper<MethodInvocationExpressionModel, MethodInvocationTree> {
	@Nonnull
	@Override
	public MethodInvocationExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull MethodInvocationTree tree) {
		Range range = extractRange(table, tree);
		List<Model> typeArguments = context.map(TypeArgumentsMapper.class, tree::getTypeArguments).getArguments();
		AbstractExpressionModel methodSelect = context.map(ExpressionMapper.class, tree.getMethodSelect());
		List<AbstractExpressionModel> arguments = tree.getArguments().stream().map(t -> context.map(ExpressionMapper.class, t)).toList();
		return new MethodInvocationExpressionModel(range, typeArguments, methodSelect, arguments);
	}
}
