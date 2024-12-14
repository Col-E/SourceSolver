package software.coley.sourcesolver.mapping;

import com.sun.source.tree.*;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AssignmentExpressionModel;
import software.coley.sourcesolver.model.LiteralExpressionModel;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.model.ParenthesizedExpressionModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

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
		if (tree instanceof IdentifierTree identifier) {
			return new NameExpressionModel(range, identifier.toString());
		}

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
		// (Foo) maybeFoo
		if (tree instanceof UnaryTree unary)
			throw new UnsupportedOperationException("TODO: UnaryTree");

		// (Foo) maybeFoo
		if (tree instanceof TypeCastTree cast)
			throw new UnsupportedOperationException("TODO: TypeCastTree");

		// foo instanceof Bar b
		if (tree instanceof InstanceOfTree instanceCheck)
			throw new UnsupportedOperationException("TODO: TypeCastTree");

		// new Foo()
		if (tree instanceof NewClassTree newClass)
			throw new UnsupportedOperationException("TODO: NewClassTree");

		// array[0]
		// array[index++]
		if (tree instanceof ArrayAccessTree arrayAccess)
			throw new UnsupportedOperationException("TODO: ArrayAccessTree");

		// flag ? case1 : case2
		if (tree instanceof ConditionalExpressionTree conditional)
			throw new UnsupportedOperationException("TODO: ConditionalExpressionTree");

		// new int[N];
		// { one, two };
		if (tree instanceof NewArrayTree array)
			return context.map(ArrayDeclarationMapper.class, array);

		// a -> true
		// () -> { ... }
		if (tree instanceof LambdaExpressionTree lambda)
			throw new UnsupportedOperationException("TODO: LambdaExpressionTree");

		// value = switch(foo) { ... }
		if (tree instanceof SwitchExpressionTree switchExpr)
			throw new UnsupportedOperationException("TODO: SwitchExpressionTree");

		// String::new
		// Objects::requireNonNull
		if (tree instanceof MemberReferenceTree memberReference)
			throw new UnsupportedOperationException("TODO: MemberReferenceTree");

		// @Foo
		// @Foo(fizz = "buzz")
		if (tree instanceof AnnotationTree annotation)
			return context.map(AnnotationUseMapper.class, annotation);

		// Any bogus input that the parser cannot fit into a good model gets
		// mapped to a literal
		if (tree instanceof ErroneousTree erroneous)
			return new LiteralExpressionModel(range, LiteralExpressionModel.Kind.ERROR, erroneous.toString());

		// Handle unknown cases as literals
		return new LiteralExpressionModel(range, LiteralExpressionModel.Kind.ERROR, tree.toString());
	}
}
