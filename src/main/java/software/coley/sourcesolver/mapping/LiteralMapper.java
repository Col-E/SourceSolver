package software.coley.sourcesolver.mapping;

import com.sun.source.tree.LiteralTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.LiteralModel;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class LiteralMapper {
	@Nonnull
	public LiteralModel map(@Nonnull EndPosTable table, @Nonnull LiteralTree tree) {
		return new LiteralModel(extractRange(table, tree), tree.toString());
	}
}
