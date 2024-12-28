package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WildcardTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.model.TypeModel;

import jakarta.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class TypeMapper implements Mapper<TypeModel, Tree> {
	@Nonnull
	@Override
	public TypeModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull Tree tree) {
		if (tree instanceof PrimitiveTypeTree primitive)
			return new TypeModel.Primitive(extractRange(table, primitive), context.map(NameMapper.class, primitive));

		if (tree instanceof IdentifierTree identifier)
			return new TypeModel.NamedObject(extractRange(table, identifier), context.map(NameMapper.class, identifier));

		if (tree instanceof ArrayTypeTree arrayType) {
			TypeModel elementType = map(context, table, arrayType.getType());
			return new TypeModel.Array(extractRange(table, arrayType), elementType);
		}

		if (tree instanceof ParameterizedTypeTree parameterizedType) {
			TypeModel identifier = map(context, table, parameterizedType.getType());
			List<TypeModel> typeParameters = parameterizedType.getTypeArguments().stream()
					.map(t -> map(context, table, t))
					.toList();
			return new TypeModel.Parameterized(extractRange(table, parameterizedType), identifier, typeParameters);
		}

		if (tree instanceof WildcardTree wildcardTree) {
			// This isn't great because the identifier spans the whole wildcard tree
			// but with how the API is structured we can't get any better data.
			NameExpressionModel identifier = context.map(NameMapper.class, wildcardTree);
			Model boundModel = wildcardTree.getBound() == null ? null : map(context, table, wildcardTree.getBound());
			return new TypeModel.Wildcard(extractRange(table, wildcardTree), identifier, boundModel);
		}

		throw new IllegalArgumentException("Unsupported variable type tree: " + tree.getClass().getSimpleName());
	}
}
