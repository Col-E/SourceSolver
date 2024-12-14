package software.coley.sourcesolver.mapping;

import com.sun.source.tree.MemberSelectTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.MemberSelectExpressionModel;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MemberSelectMapper implements Mapper<MemberSelectExpressionModel, MemberSelectTree> {
	@Nonnull
	@Override
	public MemberSelectExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull MemberSelectTree tree) {
		String name = tree.getIdentifier().toString();
		AbstractModel selectContext = context.map(ExpressionMapper.class, tree.getExpression());
		return new MemberSelectExpressionModel(extractRange(table, tree), name, selectContext);
	}
}
