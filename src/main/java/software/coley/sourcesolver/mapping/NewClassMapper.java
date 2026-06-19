package software.coley.sourcesolver.mapping;

import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.ClassModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.NewClassExpressionModel;
import software.coley.sourcesolver.util.Range;

import java.util.List;

public class NewClassMapper implements Mapper<NewClassExpressionModel, NewClassTree> {
	@Nonnull
	@Override
	public NewClassExpressionModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull NewClassTree tree) {
		Range range = extractor.get(tree);
		AbstractExpressionModel enclosing = tree.getEnclosingExpression() == null ? null : context.map(ExpressionMapper.class, tree.getEnclosingExpression());
		List<Model> typeArguments = context.map(TypeArgumentsMapper.class, tree::getTypeArguments).getArguments();
		AbstractExpressionModel identifier;
		if (tree.getIdentifier() instanceof ParameterizedTypeTree parameterizedType) {
			// Used for generic allocations:
			//  new ArrayList<>
			identifier = context.map(NameMapper.class, parameterizedType.getType());

			// The identifier's type-arguments take precedence. This is a weird design but whatever its seemingly correct.
			typeArguments = context.map(TypeArgumentsMapper.class, parameterizedType::getTypeArguments).getArguments();
		} else {
			identifier = context.map(ExpressionMapper.class, tree.getIdentifier());
		}

		List<AbstractExpressionModel> arguments = tree.getArguments().stream().map(t -> context.map(ExpressionMapper.class, t)).toList();
		ClassModel body = tree.getClassBody() == null ? null : context.map(ClassMapper.class, tree.getClassBody());
		return new NewClassExpressionModel(range, enclosing, typeArguments, identifier, arguments, body);
	}
}
