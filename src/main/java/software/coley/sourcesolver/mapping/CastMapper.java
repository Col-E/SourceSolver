package software.coley.sourcesolver.mapping;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.CastExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.UnknownExpressionModel;
import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class CastMapper implements Mapper<CastExpressionModel, TypeCastTree> {
	@Nonnull
	@Override
	public CastExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull TypeCastTree tree) {
		Range range = extractRange(table, tree);
		Model type;
		Tree typeTree = tree.getType();
		if (typeTree instanceof IdentifierTree identifier) {
			type = context.map(IdentifierMapper.class, identifier);
		} else if (typeTree instanceof PrimitiveTypeTree primitive) {
			type = context.map(TypeMapper.class, primitive);
		} else {
			type = new UnknownExpressionModel(extractRange(table, typeTree), typeTree.toString());
		}
		AbstractExpressionModel expression = context.map(ExpressionMapper.class, tree.getExpression());
		return new CastExpressionModel(range, type, expression);
	}
}
