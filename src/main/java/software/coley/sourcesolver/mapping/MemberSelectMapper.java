package software.coley.sourcesolver.mapping;

import com.sun.source.tree.MemberSelectTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.MemberSelectModel;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MemberSelectMapper implements Mapper<MemberSelectModel, MemberSelectTree> {
	@Nonnull
	@Override
	public MemberSelectModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull MemberSelectTree tree) {
		String name = tree.getIdentifier().toString();
		AbstractModel selectContext = context.map(ExpressionMapper.class, tree.getExpression());
		return new MemberSelectModel(extractRange(table, tree), name, selectContext);
	}
}
