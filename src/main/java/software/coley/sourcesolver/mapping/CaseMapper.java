package software.coley.sourcesolver.mapping;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AbstractCaseLabelModel;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AbstractStatementModel;
import software.coley.sourcesolver.model.CaseModel;
import software.coley.sourcesolver.model.Model;

import java.util.Collections;
import java.util.List;

public class CaseMapper implements Mapper<CaseModel, CaseTree> {
	@Nonnull
	@Override
	public CaseModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull CaseTree tree) {
		List<AbstractCaseLabelModel> labels = tree.getLabels() == null ? Collections.emptyList() : tree.getLabels().stream()
				.map(c -> context.map(CaseLabelMapper.class, c))
				.toList();
		List<AbstractExpressionModel> expressions = tree.getExpressions().stream()
				.map(c -> context.map(ExpressionMapper.class, c))
				.toList();
		Model body = tree.getBody() == null ? null : mapBody(context, tree.getBody());
		List<AbstractStatementModel> statements = tree.getStatements() == null ? Collections.emptyList() : tree.getStatements().stream()
				.map(c -> context.map(StatementMapper.class, c))
				.toList();
		return new CaseModel(extractor.get(tree), labels, expressions, statements, body);
	}

	@Nonnull
	private Model mapBody(@Nonnull MappingContext context, @Nonnull Tree tree) {
		if (tree instanceof ExpressionTree expression)
			return context.map(ExpressionMapper.class, expression);
		else if (tree instanceof StatementTree statementMapper)
			return context.map(StatementMapper.class, statementMapper);
		throw new IllegalStateException("Case body was not an expression or statement");
	}
}
