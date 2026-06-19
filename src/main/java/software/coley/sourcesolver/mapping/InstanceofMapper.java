package software.coley.sourcesolver.mapping;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AbstractPatternModel;
import software.coley.sourcesolver.model.DeconstructionPatternModel;
import software.coley.sourcesolver.model.InstanceofExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.UnknownExpressionModel;
import software.coley.sourcesolver.util.Range;

public class InstanceofMapper implements Mapper<InstanceofExpressionModel, InstanceOfTree> {
	@Nonnull
	@Override
	public InstanceofExpressionModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull InstanceOfTree tree) {
		Range range = extractor.get(tree);
		Model type;
		Tree typeTree = tree.getType();
		AbstractPatternModel pattern = tree.getPattern() == null ? null : context.map(PatternMapper.class, tree.getPattern());
		if (typeTree instanceof IdentifierTree identifier) {
			type = context.map(IdentifierMapper.class, identifier);
		} else if (typeTree instanceof PrimitiveTypeTree primitive) {
			type = context.map(TypeMapper.class, primitive);
		} else if (pattern instanceof DeconstructionPatternModel deconstruction) {
			type = deconstruction.getDeconstructor(); // Use the deconstructor identifier as the type
		} else {
			type = new UnknownExpressionModel(range, typeTree == null ? "<error>" : typeTree.toString());
		}
		AbstractExpressionModel expression = context.map(ExpressionMapper.class, tree.getExpression());
		return new InstanceofExpressionModel(range, expression, type, pattern);
	}
}
