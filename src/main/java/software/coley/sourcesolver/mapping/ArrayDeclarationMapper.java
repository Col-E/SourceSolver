package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.ArrayExpressionModel;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static software.coley.sourcesolver.util.Range.extractRange;

public class ArrayDeclarationMapper implements Mapper<ArrayExpressionModel, NewArrayTree> {
	@Nonnull
	@Override
	public ArrayExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull NewArrayTree tree) {
		List<AnnotationExpressionModel> annotationModels = tree.getAnnotations().stream()
				.map(anno -> context.map(AnnotationUseMapper.class, anno))
				.toList();

		List<? extends ExpressionTree> dimensions = Objects.requireNonNullElse(tree.getDimensions(), Collections.emptyList());
		List<? extends ExpressionTree> initializers = Objects.requireNonNullElse(tree.getInitializers(), Collections.emptyList());

		List<AbstractExpressionModel> dimensionModels = dimensions.stream().map(e -> context.map(ExpressionMapper.class, e)).toList();
		List<AbstractExpressionModel> initializersModels = initializers.stream().map(e -> context.map(ExpressionMapper.class, e)).toList();

		return new ArrayExpressionModel(extractRange(table, tree), dimensionModels, initializersModels, annotationModels);
	}
}
