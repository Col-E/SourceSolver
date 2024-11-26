package software.coley.sourcesolver.mapping;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.AnnotationUseModel;
import software.coley.sourcesolver.model.ArrayDeclarationModel;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static software.coley.sourcesolver.util.Range.extractRange;

public class ArrayDeclarationMapper {
	@Nonnull
	public ArrayDeclarationModel map(@Nonnull EndPosTable table, @Nonnull NewArrayTree tree) {
		List<? extends AnnotationTree> annotations = tree.getAnnotations();
		AnnotationUseMapper annotationMapper = new AnnotationUseMapper();
		List<AnnotationUseModel> annotationModels = annotations.stream()
				.map(anno -> annotationMapper.map(table, anno))
				.toList();

		List<? extends ExpressionTree> dimensions = Objects.requireNonNullElse(tree.getDimensions(), Collections.emptyList());
		List<? extends ExpressionTree> initializers = Objects.requireNonNullElse(tree.getInitializers(), Collections.emptyList());

		ExpressionMapper expressionMapper = new ExpressionMapper();
		List<AbstractModel> dimensionModels = dimensions.stream().map(e -> expressionMapper.map(table, e)).toList();
		List<AbstractModel> initializersModels = initializers.stream().map(e -> expressionMapper.map(table, e)).toList();

		return new ArrayDeclarationModel(extractRange(table, tree), dimensionModels, initializersModels, annotationModels);
	}
}
