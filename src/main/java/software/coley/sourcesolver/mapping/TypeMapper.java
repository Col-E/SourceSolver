package software.coley.sourcesolver.mapping;

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
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.model.TypeModel;
import software.coley.sourcesolver.util.Range;

import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class TypeMapper implements Mapper<TypeModel, Tree> {
	@Nonnull
	@Override
	public TypeModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull Tree tree) {
		Range range = extractRange(table, tree);
		return switch (tree) {
			case PrimitiveTypeTree primitive -> new TypeModel.Primitive(range, context.map(NameMapper.class, primitive));
			case IdentifierTree identifier -> new TypeModel.NamedObject(range, context.map(NameMapper.class, identifier));
			case ErroneousTree erroneous -> new TypeModel.NamedObject(range, context.map(NameMapper.class, erroneous));
			case MemberSelectTree select -> new TypeModel.NamedObject(range, context.map(MemberSelectMapper.class, select));
			case ArrayTypeTree arrayType -> {
				TypeModel elementType = map(context, table, arrayType.getType());
				yield new TypeModel.Array(range, elementType);
			}
			case ParameterizedTypeTree parameterizedType -> {
				TypeModel identifier = map(context, table, parameterizedType.getType());
				List<TypeModel> typeParameters = parameterizedType.getTypeArguments().stream()
						.map(t -> map(context, table, t))
						.toList();
				yield new TypeModel.Parameterized(range, identifier, typeParameters);
			}
			case WildcardTree wildcardTree -> {
				// This isn't great because the identifier spans the whole wildcard tree
				// but with how the API is structured we can't get any better data.
				NameExpressionModel identifier = context.map(NameMapper.class, wildcardTree);
				Model boundModel = wildcardTree.getBound() == null ? null : map(context, table, wildcardTree.getBound());
				yield new TypeModel.Wildcard(range, identifier, boundModel);
			}
			case UnionTypeTree unionTypeTree -> {
				List<TypeModel> alternatives = unionTypeTree.getTypeAlternatives().stream()
						.map(t -> map(context, table, t))
						.toList();
				yield new TypeModel.Union(range, alternatives.getFirst(), alternatives.subList(1, alternatives.size()));
			}
			default -> throw new IllegalArgumentException("Unsupported tree for TypeModel: " + tree.getClass().getSimpleName());
		};
	}
}
