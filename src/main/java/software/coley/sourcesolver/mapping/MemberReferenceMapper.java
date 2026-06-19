package software.coley.sourcesolver.mapping;

import com.sun.source.tree.MemberReferenceTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.MethodReferenceExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.util.Range;

import java.util.List;

public class MemberReferenceMapper implements Mapper<MethodReferenceExpressionModel, MemberReferenceTree> {
	@Nonnull
	@Override
	public MethodReferenceExpressionModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor,
	                                          @Nonnull MemberReferenceTree tree) {
		Range range = extractor.get(tree);
		MethodReferenceExpressionModel.Mode mode = switch (tree.getMode()) {
			case INVOKE -> MethodReferenceExpressionModel.Mode.INVOKE;
			case NEW -> MethodReferenceExpressionModel.Mode.NEW;
		};
		NameExpressionModel name = new NameExpressionModel(Range.UNKNOWN, tree.getName().toString());
		AbstractExpressionModel qualifier = context.map(ExpressionMapper.class, tree.getQualifierExpression());
		List<Model> typeArguments = context.map(TypeArgumentsMapper.class, tree::getTypeArguments).getArguments();
		return new MethodReferenceExpressionModel(range, mode, qualifier, name, typeArguments);
	}
}
