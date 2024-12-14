package software.coley.sourcesolver.mapping;

import com.sun.source.tree.*;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.*;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.lang.model.element.Name;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class StatementMapper implements Mapper<AbstractStatementModel, StatementTree> {
	@Nonnull
	@Override
	public AbstractStatementModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull StatementTree tree) {
		Range range = extractRange(table, tree);
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
			AbstractStatementModel statement = map(context, table, doWhileLoopTree.getStatement());
			return new DoWhileLoopStatementModel(range, condition, statement);
		}
		if (tree instanceof EmptyStatementTree) {
			return new EmptyStatementModel(range);
		}
		if (tree instanceof EnhancedForLoopTree enhancedForLoopTree) {
			VariableModel variable = context.map(VariableMapper.class, enhancedForLoopTree.getVariable());
			AbstractExpressionModel expression = context.map(ExpressionMapper.class, enhancedForLoopTree.getExpression());
			AbstractStatementModel statement = map(context, table, enhancedForLoopTree.getStatement());
			return new EnhancedForLoopStatementModel(range, variable, expression, statement);
		}
		if (tree instanceof ExpressionStatementTree expressionStatementTree) {
			AbstractExpressionModel expression = context.map(ExpressionMapper.class, expressionStatementTree.getExpression());
			return new ExpressionStatementModel(range, expression);
		}
		if (tree instanceof ForLoopTree forLoopTree) {
			List<AbstractStatementModel> initializerStatements = forLoopTree.getInitializer().stream()
					.map(s -> map(context, table, s))
					.toList();
			List<AbstractStatementModel> updateStatements = forLoopTree.getUpdate().stream()
					.map(s -> map(context, table, s))
					.toList();
			AbstractExpressionModel condition = context.map(ExpressionMapper.class, forLoopTree.getCondition());
			AbstractStatementModel statement = map(context, table, forLoopTree.getStatement());
			return new ForLoopStatementModel(range, initializerStatements, updateStatements, condition, statement);
		}
		if (tree instanceof IfTree ifTree) {
			AbstractExpressionModel condition = context.map(ExpressionMapper.class, ifTree.getCondition());
			AbstractStatementModel thenStatement = map(context, table, ifTree.getThenStatement());
			AbstractStatementModel elseStatement = ifTree.getElseStatement() == null ? null : map(context, table, ifTree.getElseStatement());
			return new IfStatementModel(range, condition, thenStatement, elseStatement);
		}
		if (tree instanceof LabeledStatementTree labeledStatementTree) {
			Name targetLabel = labeledStatementTree.getLabel();
			AbstractStatementModel statement = map(context, table, labeledStatementTree.getStatement());
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
			AbstractStatementModel statement = map(context, table, whileLoopTree.getStatement());
			return new WhileLoopStatementModel(range, condition, statement);
		}
		if (tree instanceof YieldTree yieldTree) {
			return new YieldStatementModel(range, context.map(ExpressionMapper.class, yieldTree.getValue()));
		}

		// Generic fallback
		return new UnknownStatementModel(extractRange(table, tree));
	}
}
