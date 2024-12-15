package software.coley.sourcesolver.mapping;

import com.sun.source.tree.*;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.ArrayAccessExpressionModel;
import software.coley.sourcesolver.model.AssignmentExpressionModel;
import software.coley.sourcesolver.model.CaseModel;
import software.coley.sourcesolver.model.ConditionalExpressionModel;
import software.coley.sourcesolver.model.ParenthesizedExpressionModel;
import software.coley.sourcesolver.model.SwitchExpressionModel;
import software.coley.sourcesolver.model.UnknownExpressionModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class ExpressionMapper implements Mapper<AbstractExpressionModel, ExpressionTree> {
	@Nonnull
	@Override
	public AbstractExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull ExpressionTree tree) {
		Range range = extractRange(table, tree);

		// Anything in parentheses
		if (tree instanceof ParenthesizedTree parenthesized)
			return new ParenthesizedExpressionModel(range, map(context, table, parenthesized.getExpression()));

		// Strings, integers, floats, etc
		if (tree instanceof LiteralTree literal)
			return context.map(LiteralMapper.class, literal);

		// ???
		if (tree instanceof IdentifierTree identifier)
			return context.map(IdentifierMapper.class, identifier);

		// Enum.name
		// Constants.MY_CONSTANT
		if (tree instanceof MemberSelectTree memberAccess)
			return context.map(MemberSelectMapper.class, memberAccess);

		// Util.doUtility()
		if (tree instanceof MethodInvocationTree methodInvoke)
			return context.map(MethodInvocationMapper.class, methodInvoke);

		// foo = expression
		if (tree instanceof AssignmentTree assignment) {
			AbstractExpressionModel variable = map(context, table, assignment.getVariable());
			AbstractExpressionModel expression = map(context, table, assignment.getExpression());
			return new AssignmentExpressionModel(range, variable, expression, AssignmentExpressionModel.Operator.SET);
		}

		// foo += value
		if (tree instanceof CompoundAssignmentTree assignment) {
			AbstractExpressionModel variable = map(context, table, assignment.getVariable());
			AbstractExpressionModel expression = map(context, table, assignment.getExpression());
			AssignmentExpressionModel.Operator operator = switch (tree.getKind()) {
				case PLUS_ASSIGNMENT -> AssignmentExpressionModel.Operator.PLUS;
				case MINUS_ASSIGNMENT -> AssignmentExpressionModel.Operator.MINUS;
				case MULTIPLY_ASSIGNMENT -> AssignmentExpressionModel.Operator.MULTIPLY;
				case DIVIDE_ASSIGNMENT -> AssignmentExpressionModel.Operator.DIVIDE;
				case REMAINDER_ASSIGNMENT -> AssignmentExpressionModel.Operator.REMAINDER;
				case OR_ASSIGNMENT -> AssignmentExpressionModel.Operator.BIT_OR;
				case AND_ASSIGNMENT -> AssignmentExpressionModel.Operator.BIT_AND;
				case XOR_ASSIGNMENT -> AssignmentExpressionModel.Operator.BIT_XOR;
				case LEFT_SHIFT_ASSIGNMENT -> AssignmentExpressionModel.Operator.SHIFT_LEFT;
				case RIGHT_SHIFT_ASSIGNMENT -> AssignmentExpressionModel.Operator.SHIFT_RIGHT;
				case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT -> AssignmentExpressionModel.Operator.SHIFT_RIGHT_UNSIGNED;
				default -> AssignmentExpressionModel.Operator.UNKNOWN;
			};
			return new AssignmentExpressionModel(range, variable, expression, operator);
		}

		// five * two
		// bit << shift
		// one || another
		if (tree instanceof BinaryTree binary)
			return context.map(BinaryMapper.class, binary);

		// x++
		if (tree instanceof UnaryTree unary)
			return context.map(UnaryMapper.class, unary);

		// (Foo) maybeFoo
		if (tree instanceof TypeCastTree cast)
			return context.map(CastMapper.class, cast);

		// foo instanceof Bar b
		if (tree instanceof InstanceOfTree instanceCheck)
			return context.map(InstanceofMapper.class, instanceCheck);

		// new Foo()
		if (tree instanceof NewClassTree newClass)
			return context.map(NewClassMapper.class, newClass);

		// array[0]
		// array[index++]
		if (tree instanceof ArrayAccessTree arrayAccess)
			return new ArrayAccessExpressionModel(range,
					map(context, table, arrayAccess.getExpression()),
					map(context, table, arrayAccess.getIndex()));

		// condition ? case1 : case2
		if (tree instanceof ConditionalExpressionTree conditional)
			return new ConditionalExpressionModel(range,
					map(context, table, conditional.getCondition()),
					map(context, table, conditional.getTrueExpression()),
					map(context, table, conditional.getFalseExpression()));

		// new int[N];
		// { one, two };
		if (tree instanceof NewArrayTree array)
			return context.map(ArrayDeclarationMapper.class, array);

		// a -> true
		// () -> { ... }
		if (tree instanceof LambdaExpressionTree lambda)
			return context.map(LambdaMapper.class, lambda);

		// value = switch(foo) { ... }
		if (tree instanceof SwitchExpressionTree switchExpr) {
			AbstractExpressionModel expression = context.map(ExpressionMapper.class, switchExpr.getExpression());
			List<CaseModel> cases = switchExpr.getCases().stream()
					.map(c -> context.map(CaseMapper.class, c))
					.toList();
			return new SwitchExpressionModel(range, expression, cases);
		}

		// String::new
		// Objects::requireNonNull
		if (tree instanceof MemberReferenceTree memberReference)
			return context.map(MemberReferenceMapper.class, memberReference);

		// @Foo
		// @Foo(fizz = "buzz")
		if (tree instanceof AnnotationTree annotation)
			return context.map(AnnotationUseMapper.class, annotation);

		// Handle unknown cases or errors as unknowns which toString the content
		return new UnknownExpressionModel(range, tree.toString());
	}
}
