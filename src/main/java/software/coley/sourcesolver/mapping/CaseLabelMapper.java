package software.coley.sourcesolver.mapping;

import com.sun.source.tree.CaseLabelTree;
import com.sun.source.tree.ConstantCaseLabelTree;
import com.sun.source.tree.PatternCaseLabelTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractCaseLabelModel;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AbstractPatternModel;
import software.coley.sourcesolver.model.ConstCaseLabelModel;
import software.coley.sourcesolver.model.DefaultCaseLabelModel;
import software.coley.sourcesolver.model.PatternCaseLabelModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class CaseLabelMapper implements Mapper<AbstractCaseLabelModel, CaseLabelTree> {
	@Nonnull
	@Override
	public AbstractCaseLabelModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull CaseLabelTree tree) {
		Range range = extractRange(table, tree);
		if (tree instanceof ConstantCaseLabelTree constLabel) {
			AbstractExpressionModel constant = context.map(ExpressionMapper.class, constLabel.getConstantExpression());
			return new ConstCaseLabelModel(range, constant);
		} else if (tree instanceof PatternCaseLabelTree patternLabel) {
			AbstractPatternModel patternModel = context.map(PatternMapper.class, patternLabel.getPattern());
			return new PatternCaseLabelModel(range, patternModel);
		}
		return new DefaultCaseLabelModel(range);
	}
}
