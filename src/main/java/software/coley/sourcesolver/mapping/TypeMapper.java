package software.coley.sourcesolver.mapping;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.WildcardTree;
import com.sun.tools.javac.tree.EndPosTable;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.model.TypeModel;
import software.coley.sourcesolver.model.TypeModel.Annotated;
import software.coley.sourcesolver.model.TypeModel.Array;
import software.coley.sourcesolver.model.TypeModel.NamedObject;
import software.coley.sourcesolver.model.TypeModel.Parameterized;
import software.coley.sourcesolver.model.TypeModel.Primitive;
import software.coley.sourcesolver.model.TypeModel.Union;
import software.coley.sourcesolver.model.TypeModel.Wildcard;
import software.coley.sourcesolver.util.Range;

import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class TypeMapper implements Mapper<TypeModel, Tree> {
	@Nonnull
	@Override
	public TypeModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull Tree tree) {
		// The logic is extracted so that some cases can manipulate the range value they use
		// to construct type models. Namely, because of type annotations.
		Range range = extractRange(table, tree);
		return mapImpl(context, table, tree, range);
	}

	@Nonnull
	private TypeModel mapImpl(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull Tree tree, @Nonnull Range range) {
		return switch (tree) {
			case PrimitiveTypeTree primitive -> new Primitive(range, context.map(NameMapper.class, primitive));
			case IdentifierTree identifier -> new NamedObject(range, context.map(NameMapper.class, identifier));
			case ErroneousTree erroneous -> new NamedObject(range, context.map(NameMapper.class, erroneous));
			case MemberSelectTree select -> new NamedObject(range, context.map(MemberSelectMapper.class, select));
			case ArrayTypeTree arrayType -> {
				TypeModel elementType = map(context, table, arrayType.getType());
				yield new Array(range, elementType);
			}
			case ParameterizedTypeTree parameterizedType -> {
				TypeModel identifier = map(context, table, parameterizedType.getType());
				List<TypeModel> typeParameters = parameterizedType.getTypeArguments().stream()
						.map(t -> map(context, table, t))
						.toList();
				yield new Parameterized(range, identifier, typeParameters);
			}
			case WildcardTree wildcardTree -> {
				// This isn't great because the identifier spans the whole wildcard tree
				// but with how the API is structured we can't get any better data.
				NameExpressionModel identifier = context.map(NameMapper.class, wildcardTree);
				Model boundModel = wildcardTree.getBound() == null ? null : map(context, table, wildcardTree.getBound());
				yield new Wildcard(range, identifier, boundModel);
			}
			case UnionTypeTree unionTypeTree -> {
				List<TypeModel> alternatives = unionTypeTree.getTypeAlternatives().stream()
						.map(t -> map(context, table, t))
						.toList();
				yield new Union(range, alternatives.getFirst(), alternatives.subList(1, alternatives.size()));
			}
			case AnnotatedTypeTree annotatedTypeTree -> {
				List<AnnotationExpressionModel> annotationModels = annotatedTypeTree.getAnnotations().stream()
						.map(anno -> context.map(AnnotationUseMapper.class, anno))
						.toList();

				// Annotations mess with the range of the underlying type,
				// so we need to manipulate it a bit if annotations are present.
				Range underlyingRange = range.shrink(annotationModels);
				TypeModel type = mapImpl(context, table, annotatedTypeTree.getUnderlyingType(), underlyingRange);

				// The annotated type uses the original full range though.
				yield new Annotated(range, type, annotationModels);
			}
			default -> {
				String name = tree.getClass().getSimpleName();
				throw new IllegalArgumentException("Unsupported tree for TypeModel: " + name);
			}
		};
	}
}
