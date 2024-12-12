package software.coley.sourcesolver.mapping;

import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractStatementModel;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class StatementMapper {
	@Nonnull
	public AbstractStatementModel map(@Nonnull EndPosTable table, @Nonnull StatementTree tree) {
		return new AbstractStatementModel(extractRange(table, tree));
	}
}
