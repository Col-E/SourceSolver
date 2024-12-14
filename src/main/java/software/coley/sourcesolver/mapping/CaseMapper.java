package software.coley.sourcesolver.mapping;

import com.sun.source.tree.CaseTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AbstractStatementModel;
import software.coley.sourcesolver.model.CaseModel;

import javax.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class CaseMapper implements Mapper<CaseModel, CaseTree> {
	@Nonnull
	@Override
	public CaseModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull CaseTree tree) {
		List<AbstractExpressionModel> expressions = tree.getExpressions().stream()
				.map(c -> context.map(ExpressionMapper.class, c))
				.toList();
		List<AbstractStatementModel> statements = tree.getStatements().stream()
				.map(c -> context.map(StatementMapper.class, c))
				.toList();
		return new CaseModel(extractRange(table, tree), expressions, statements);
	}
}
