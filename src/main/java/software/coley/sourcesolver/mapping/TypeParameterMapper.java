package software.coley.sourcesolver.mapping;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.TypeParameterModel;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class TypeParameterMapper implements Mapper<TypeParameterModel, TypeParameterTree> {
	@Nonnull
	@Override
	public TypeParameterModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull TypeParameterTree tree) {
		List<AnnotationExpressionModel> annotationModels = tree.getAnnotations().stream()
				.map(anno -> context.map(AnnotationUseMapper.class, anno))
				.toList();
		// TODO: What tree type are the bounds in practice?
		List<? extends Tree> bounds = tree.getBounds();
		String name = tree.getName().toString();
		return new TypeParameterModel(extractRange(table, tree), name, Collections.emptyList(), annotationModels);
	}
}
