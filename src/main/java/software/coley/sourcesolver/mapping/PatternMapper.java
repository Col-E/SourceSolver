package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.DeconstructionPatternTree;
import com.sun.source.tree.PatternTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AbstractPatternModel;
import software.coley.sourcesolver.model.BindingPatternModel;
import software.coley.sourcesolver.model.DeconstructionPatternModel;
import software.coley.sourcesolver.model.UnknownPatternModel;
import software.coley.sourcesolver.model.VariableModel;
import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class PatternMapper implements Mapper<AbstractPatternModel, PatternTree> {
	@Nonnull
	@Override
	public AbstractPatternModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull PatternTree tree) {
		Range range = extractRange(table, tree);
		if (tree instanceof BindingPatternTree bindingTree) {
			VariableModel variable = context.map(VariableMapper.class, bindingTree.getVariable());
			return new BindingPatternModel(range, variable);
		} else if (tree instanceof DeconstructionPatternTree deconstructionTree) {
			AbstractExpressionModel deconstructor = context.map(ExpressionMapper.class, deconstructionTree.getDeconstructor());
			List<AbstractPatternModel> nestedPatterns = deconstructionTree.getNestedPatterns().stream()
					.map(p -> context.map(PatternMapper.class, p))
					.toList();
			return new DeconstructionPatternModel(range, deconstructor, nestedPatterns);
		}
		/*
		// JDK 17 preview feature implementations
		 else if (tree instanceof GuardedPatternTree guardedTree) {
			AbstractPatternModel pattern = map(context, table, guardedTree.getPattern());
			AbstractExpressionModel expression = context.map(ExpressionMapper.class, guardedTree.getExpression());
			return new GuardedPatternModel(range, pattern, expression);
		} else if (tree instanceof ParenthesizedPatternTree parenthesizedTree) {
			AbstractPatternModel pattern = map(context, table, parenthesizedTree.getPattern());
			return new ParenthesizedPatternModel(range, pattern);
		}
		// JDK 21 preview feature implementations
		else if (tree instanceof AnyPatternTree) {
			return new AnyPatternModel(range);
		}
		 */
		return new UnknownPatternModel(range, tree.toString());
	}
}
