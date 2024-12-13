package software.coley.sourcesolver.mapping;

import com.sun.source.tree.*;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.LiteralModel;
import software.coley.sourcesolver.model.NameModel;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class ExpressionMapper implements Mapper<AbstractModel, ExpressionTree> {
	@Nonnull
	@Override
	public AbstractModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull ExpressionTree tree) {
		// Anything in parentheses
		if (tree instanceof ParenthesizedTree parenthesized)
			throw new UnsupportedOperationException("TODO: ParenthesizedTree");

		// Strings, integers, floats, etc
		if (tree instanceof LiteralTree literal)
			return context.map(LiteralMapper.class, literal);

		// ???
		if (tree instanceof IdentifierTree identifier)
			return new NameModel(extractRange(table, identifier), identifier.toString());

		// Enum.name
		// Constants.MY_CONSTANT
		if (tree instanceof MemberSelectTree memberAccess)
			return context.map(MemberSelectMapper.class, memberAccess);

		// Util.doUtility()
		if (tree instanceof MethodInvocationTree methodInvoke)
			throw new UnsupportedOperationException("TODO: MethodInvocationTree");

		// foo = expression
		if (tree instanceof AssignmentTree assignment)
			throw new UnsupportedOperationException("TODO: AssignmentTree");

		// five * two
		// bit << shift
		// one || another
		if (tree instanceof BinaryTree binary)
			throw new UnsupportedOperationException("TODO: BinaryTree");

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
			return new LiteralModel(extractRange(table, erroneous), LiteralModel.Kind.ERROR, erroneous.toString());

		// Handle unknown cases as literals
		return new LiteralModel(extractRange(table, tree), LiteralModel.Kind.ERROR, tree.toString());
	}
}
