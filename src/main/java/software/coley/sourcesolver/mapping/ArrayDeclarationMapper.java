package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.ArrayDeclarationExpressionModel;
import software.coley.sourcesolver.model.TypeModel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ArrayDeclarationMapper implements Mapper<ArrayDeclarationExpressionModel, NewArrayTree> {
	@Nonnull
	@Override
	public ArrayDeclarationExpressionModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull NewArrayTree tree) {
		List<AnnotationExpressionModel> annotationModels = tree.getAnnotations().stream()
				.map(anno -> context.map(AnnotationUseMapper.class, anno))
				.toList();

		List<? extends ExpressionTree> dimensions = Objects.requireNonNullElse(tree.getDimensions(), Collections.emptyList());
		List<? extends ExpressionTree> initializers = Objects.requireNonNullElse(tree.getInitializers(), Collections.emptyList());

		List<AbstractExpressionModel> dimensionModels = dimensions.stream().map(e -> context.map(ExpressionMapper.class, e)).toList();
		List<AbstractExpressionModel> initializersModels = initializers.stream().map(e -> context.map(ExpressionMapper.class, e)).toList();

		TypeModel type = context.mapOr(TypeMapper.class, tree.getType(), TypeModel::newVar);

		return new ArrayDeclarationExpressionModel(extractor.get(tree), type, dimensionModels, initializersModels, annotationModels);
	}
}
