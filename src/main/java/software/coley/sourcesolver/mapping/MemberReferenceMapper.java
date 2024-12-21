package software.coley.sourcesolver.mapping;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.MethodReferenceExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MemberReferenceMapper implements Mapper<MethodReferenceExpressionModel, MemberReferenceTree> {
	@Nonnull
	@Override
	public MethodReferenceExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table,
	                                          @Nonnull MemberReferenceTree tree) {
		Range range = extractRange(table, tree);
		MethodReferenceExpressionModel.Mode mode = switch (tree.getMode()) {
			case INVOKE -> MethodReferenceExpressionModel.Mode.INVOKE;
			case NEW -> MethodReferenceExpressionModel.Mode.NEW;
		};
		NameExpressionModel name = new NameExpressionModel(Range.UNKNOWN, tree.getName().toString());
		AbstractExpressionModel qualifier = context.map(ExpressionMapper.class, tree.getQualifierExpression());
		List<Model> typeArguments = context.map(TypeArgumentsMapper.class, tree::getTypeArguments).getArguments();
		return new MethodReferenceExpressionModel(range, mode, qualifier, name, typeArguments);
	}
}
