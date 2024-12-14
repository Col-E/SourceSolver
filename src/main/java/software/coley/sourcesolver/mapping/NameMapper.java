package software.coley.sourcesolver.mapping;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.NameExpressionModel;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class NameMapper implements Mapper<NameExpressionModel, Tree> {
	@Nonnull
	@Override
	public NameExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull Tree tree) {
		return new NameExpressionModel(extractRange(table, tree), tree.toString());
	}
}
