package software.coley.sourcesolver.mapping;

import com.sun.source.tree.AssertTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.YieldTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AbstractStatementModel;
import software.coley.sourcesolver.model.AssertStatementModel;
import software.coley.sourcesolver.model.BreakStatementModel;
import software.coley.sourcesolver.model.CaseModel;
import software.coley.sourcesolver.model.ContinueStatementModel;
import software.coley.sourcesolver.model.DoWhileLoopStatementModel;
import software.coley.sourcesolver.model.EmptyStatementModel;
import software.coley.sourcesolver.model.EnhancedForLoopStatementModel;
import software.coley.sourcesolver.model.ErroneousExpressionStatementModel;
import software.coley.sourcesolver.model.ErroneousModel;
import software.coley.sourcesolver.model.ExpressionStatementModel;
import software.coley.sourcesolver.model.ForLoopStatementModel;
import software.coley.sourcesolver.model.IfStatementModel;
import software.coley.sourcesolver.model.LabeledStatementModel;
import software.coley.sourcesolver.model.ReturnStatementModel;
import software.coley.sourcesolver.model.SwitchStatementModel;
import software.coley.sourcesolver.model.SynchronizedStatementModel;
import software.coley.sourcesolver.model.ThrowStatementModel;
import software.coley.sourcesolver.model.UnknownStatementModel;
import software.coley.sourcesolver.model.VariableModel;
import software.coley.sourcesolver.model.WhileLoopStatementModel;
import software.coley.sourcesolver.model.YieldStatementModel;
import software.coley.sourcesolver.util.Range;

import javax.lang.model.element.Name;
import java.util.List;

public class StatementMapper implements Mapper<AbstractStatementModel, StatementTree> {
	@Nonnull
	@Override
	public AbstractStatementModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull StatementTree tree) {
		Range range = extractor.get(tree);
		if (tree instanceof AssertTree assertTree) {
			AbstractExpressionModel condition = context.map(ExpressionMapper.class, assertTree.getCondition());
			AbstractExpressionModel detail = assertTree.getDetail() == null ? null : context.map(ExpressionMapper.class, assertTree.getDetail());
			return new AssertStatementModel(range, condition, detail);
		}
		if (tree instanceof BlockTree blockTree) {
			return context.map(BlockMapper.class, blockTree);
		}
		if (tree instanceof BreakTree breakTree) {
			Name targetLabel = breakTree.getLabel();
			return new BreakStatementModel(range, targetLabel == null ? null : targetLabel.toString());
		}
		if (tree instanceof ClassTree classTree) {
			return context.map(ClassMapper.class, classTree);
		}
		if (tree instanceof ContinueTree continueTree) {
			Name targetLabel = continueTree.getLabel();
			return new ContinueStatementModel(range, targetLabel == null ? null : targetLabel.toString());
		}
		if (tree instanceof DoWhileLoopTree doWhileLoopTree) {
			AbstractExpressionModel condition = context.map(ExpressionMapper.class, doWhileLoopTree.getCondition());
			AbstractStatementModel statement = map(context, extractor, doWhileLoopTree.getStatement());
			return new DoWhileLoopStatementModel(range, condition, statement);
		}
		if (tree instanceof EmptyStatementTree) {
			return new EmptyStatementModel(range);
		}
		if (tree instanceof EnhancedForLoopTree enhancedForLoopTree) {
			VariableModel variable = context.map(VariableMapper.class, enhancedForLoopTree.getVariable());
			AbstractExpressionModel expression = context.map(ExpressionMapper.class, enhancedForLoopTree.getExpression());
			AbstractStatementModel statement = map(context, extractor, enhancedForLoopTree.getStatement());
			return new EnhancedForLoopStatementModel(range, variable, expression, statement);
		}
		if (tree instanceof ExpressionStatementTree expressionStatementTree) {
			AbstractExpressionModel expression = context.map(ExpressionMapper.class, expressionStatementTree.getExpression());
			return (expression instanceof ErroneousModel) ?
					new ErroneousExpressionStatementModel(range, expression) :
					new ExpressionStatementModel(range, expression);
		}
		if (tree instanceof ForLoopTree forLoopTree) {
			List<AbstractStatementModel> initializerStatements = forLoopTree.getInitializer().stream()
					.map(s -> map(context, extractor, s))
					.toList();
			List<AbstractStatementModel> updateStatements = forLoopTree.getUpdate().stream()
					.map(s -> map(context, extractor, s))
					.toList();
			AbstractExpressionModel condition = context.map(ExpressionMapper.class, forLoopTree.getCondition());
			AbstractStatementModel statement = map(context, extractor, forLoopTree.getStatement());
			return new ForLoopStatementModel(range, initializerStatements, updateStatements, condition, statement);
		}
		if (tree instanceof IfTree ifTree) {
			AbstractExpressionModel condition = context.map(ExpressionMapper.class, ifTree.getCondition());
			AbstractStatementModel thenStatement = map(context, extractor, ifTree.getThenStatement());
			AbstractStatementModel elseStatement = ifTree.getElseStatement() == null ? null : map(context, extractor, ifTree.getElseStatement());
			return new IfStatementModel(range, condition, thenStatement, elseStatement);
		}
		if (tree instanceof LabeledStatementTree labeledStatementTree) {
			Name targetLabel = labeledStatementTree.getLabel();
			AbstractStatementModel statement = map(context, extractor, labeledStatementTree.getStatement());
			return new LabeledStatementModel(range, targetLabel == null ? null : targetLabel.toString(), statement);
		}
		if (tree instanceof ReturnTree returnTree) {
			AbstractExpressionModel expression = returnTree.getExpression() == null ? null : context.map(ExpressionMapper.class, returnTree.getExpression());
			return new ReturnStatementModel(range, expression);
		}
		if (tree instanceof SwitchTree switchTree) {
			AbstractExpressionModel expression = context.map(ExpressionMapper.class, switchTree.getExpression());
			List<CaseModel> cases = switchTree.getCases().stream()
					.map(c -> context.map(CaseMapper.class, c))
					.toList();
			return new SwitchStatementModel(range, expression, cases);
		}
		if (tree instanceof SynchronizedTree synchronizedTree) {
			return new SynchronizedStatementModel(range,
					context.map(ExpressionMapper.class, synchronizedTree.getExpression()),
					context.map(BlockMapper.class, synchronizedTree.getBlock()));
		}
		if (tree instanceof ThrowTree throwTree) {
			return new ThrowStatementModel(range, context.map(ExpressionMapper.class, throwTree.getExpression()));
		}
		if (tree instanceof TryTree tryTree) {
			return context.map(TryMapper.class, tryTree);
		}
		if (tree instanceof VariableTree variableTree) {
			return context.map(VariableMapper.class, variableTree);
		}
		if (tree instanceof WhileLoopTree whileLoopTree) {
			AbstractExpressionModel condition = context.map(ExpressionMapper.class, whileLoopTree.getCondition());
			AbstractStatementModel statement = map(context, extractor, whileLoopTree.getStatement());
			return new WhileLoopStatementModel(range, condition, statement);
		}
		if (tree instanceof YieldTree yieldTree) {
			return new YieldStatementModel(range, context.map(ExpressionMapper.class, yieldTree.getValue()));
		}

		// Generic fallback
		return new UnknownStatementModel(extractor.get(tree), tree.toString());
	}
}
