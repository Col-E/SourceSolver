package software.coley.sourcesolver.mapping;

import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractStatementModel;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class StatementMapper implements Mapper<AbstractStatementModel, StatementTree> {
	@Nonnull
	@Override
	public AbstractStatementModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull StatementTree tree) {
		return new AbstractStatementModel(extractRange(table, tree));
	}
}
