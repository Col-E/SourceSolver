package software.coley.sourcesolver.mapping;

import com.sun.source.tree.CaseLabelTree;
import com.sun.source.tree.ConstantCaseLabelTree;
import com.sun.source.tree.PatternCaseLabelTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AbstractCaseLabelModel;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AbstractPatternModel;
import software.coley.sourcesolver.model.ConstCaseLabelModel;
import software.coley.sourcesolver.model.DefaultCaseLabelModel;
import software.coley.sourcesolver.model.PatternCaseLabelModel;
import software.coley.sourcesolver.util.Range;

public class CaseLabelMapper implements Mapper<AbstractCaseLabelModel, CaseLabelTree> {
	@Nonnull
	@Override
	public AbstractCaseLabelModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull CaseLabelTree tree) {
		Range range = extractor.get(tree);
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
