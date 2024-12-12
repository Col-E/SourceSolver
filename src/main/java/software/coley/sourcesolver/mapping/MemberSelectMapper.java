package software.coley.sourcesolver.mapping;

import com.sun.source.tree.MemberSelectTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.MemberSelectModel;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MemberSelectMapper {
	@Nonnull
	public MemberSelectModel map(@Nonnull EndPosTable table, @Nonnull MemberSelectTree tree) {
		String name = tree.getIdentifier().toString();
		AbstractModel context = new ExpressionMapper().map(table, tree.getExpression());
		return new MemberSelectModel(extractRange(table, tree), name, context);
	}
}
