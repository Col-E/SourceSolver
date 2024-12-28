package software.coley.sourcesolver.mapping;

import com.sun.source.tree.IdentifierTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.NameExpressionModel;

import jakarta.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class IdentifierMapper implements Mapper<NameExpressionModel, IdentifierTree> {
	@Nonnull
	@Override
	public NameExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull IdentifierTree tree) {
		return new NameExpressionModel(extractRange(table, tree), tree.toString());
	}
}
