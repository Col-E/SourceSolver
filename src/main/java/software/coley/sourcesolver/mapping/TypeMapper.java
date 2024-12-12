package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WildcardTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.NameModel;
import software.coley.sourcesolver.model.TypeModel;

import javax.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class TypeMapper {
	@Nonnull
	public TypeModel map(@Nonnull EndPosTable table, @Nonnull Tree tree) {
		if (tree instanceof PrimitiveTypeTree primitive)
			return new TypeModel.Primitive(extractRange(table, primitive), new NameMapper().map(table, primitive));

		if (tree instanceof IdentifierTree identifier)
			return new TypeModel.NamedObject(extractRange(table, identifier), new NameMapper().map(table, identifier));

		if (tree instanceof ArrayTypeTree arrayType) {
			TypeModel elementType = map(table, arrayType.getType());
			return new TypeModel.Array(extractRange(table, arrayType), elementType);
		}

		if (tree instanceof ParameterizedTypeTree parameterizedType) {
			TypeModel identifier = map(table, parameterizedType.getType());
			List<TypeModel> typeParameters = parameterizedType.getTypeArguments().stream()
					.map(t -> map(table, t))
					.toList();
			return new TypeModel.Parameterized(extractRange(table, parameterizedType), identifier, typeParameters);
		}

		if (tree instanceof WildcardTree wildcardTree) {
			NameModel identifier = new NameMapper().map(table, wildcardTree);
			AbstractModel boundModel = wildcardTree.getBound() == null ? null : map(table, wildcardTree.getBound());
			return new TypeModel.Wildcard(extractRange(table, wildcardTree), identifier, boundModel);
		}

		throw new IllegalArgumentException("Unsupported variable type tree: " + tree.getClass().getSimpleName());
	}
}
