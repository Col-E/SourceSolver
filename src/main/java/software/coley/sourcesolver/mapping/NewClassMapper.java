package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.ClassModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.NewClassExpressionModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class NewClassMapper implements Mapper<NewClassExpressionModel, NewClassTree> {
	@Nonnull
	@Override
	public NewClassExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull NewClassTree tree) {
		Range range = extractRange(table, tree);
		AbstractExpressionModel enclosing = tree.getEnclosingExpression() == null ? null : context.map(ExpressionMapper.class, tree.getEnclosingExpression());
		List<Model> typeArguments = context.map(TypeArgumentsMapper.class, tree::getTypeArguments).getArguments();
		AbstractExpressionModel identifier;
		if (tree.getIdentifier() instanceof ParameterizedTypeTree parameterizedType) {
			// Used for generic allocations:
			//  new ArrayList<>
			identifier = context.map(NameMapper.class, parameterizedType.getType());
		} else {
			identifier = context.map(ExpressionMapper.class, tree.getIdentifier());
		}

		List<AbstractExpressionModel> arguments = tree.getArguments().stream().map(t -> context.map(ExpressionMapper.class, t)).toList();
		ClassModel body = tree.getClassBody() == null ? null : context.map(ClassMapper.class, tree.getClassBody());
		return new NewClassExpressionModel(range, enclosing, typeArguments, identifier, arguments, body);
	}
}
