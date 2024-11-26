package software.coley.sourcesolver.mapping;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.NameModel;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class NameMapper {
	@Nonnull
	public NameModel map(@Nonnull EndPosTable table, @Nonnull Tree tree) {
		return new NameModel(extractRange(table, tree), tree.toString());
	}
}
