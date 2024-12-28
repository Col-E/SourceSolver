package software.coley.sourcesolver.mapping;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.TypeParameterModel;

import jakarta.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class TypeParameterMapper implements Mapper<TypeParameterModel, TypeParameterTree> {
	@Nonnull
	@Override
	public TypeParameterModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull TypeParameterTree tree) {
		List<AnnotationExpressionModel> annotationModels = tree.getAnnotations().stream()
				.map(anno -> context.map(AnnotationUseMapper.class, anno))
				.toList();
		List<Model> bounds = tree.getBounds().stream()
				.map(b -> {
					if (b instanceof IdentifierTree identifier)
						return context.map(IdentifierMapper.class, identifier);
					else if (b instanceof ParameterizedTypeTree parameterizedType)
						return context.map(TypeMapper.class, parameterizedType);
					return (Model) context.map(NameMapper.class, b);
				}).toList();
		String name = tree.getName().toString();
		return new TypeParameterModel(extractRange(table, tree), name, bounds, annotationModels);
	}
}
