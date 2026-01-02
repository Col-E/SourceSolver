package software.coley.sourcesolver.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.util.Range;

import java.util.List;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class MethodInvocationExpressionModel extends AbstractExpressionModel {
	private final List<Model> typeArguments;
	private final AbstractExpressionModel methodSelect;
	private final List<AbstractExpressionModel> arguments;

	public MethodInvocationExpressionModel(@Nonnull Range range, @Nonnull List<Model> typeArguments,
	                                       @Nonnull AbstractExpressionModel methodSelect, @Nonnull List<AbstractExpressionModel> arguments) {
		super(range, of(typeArguments), of(methodSelect), of(arguments));
		this.typeArguments = typeArguments;
		this.methodSelect = methodSelect;
		this.arguments = arguments;
	}

	@Nonnull
	public List<Model> getTypeArguments() {
		return typeArguments;
	}

	/**
	 * @return Expression including the calling context expression, and method name.
	 *
	 * @see NameExpressionModel
	 * @see MemberSelectExpressionModel
	 */
	@Nonnull
	public AbstractExpressionModel getMethodSelect() {
		return methodSelect;
	}

	@Nonnull
	public List<AbstractExpressionModel> getArguments() {
		return arguments;
	}

	/**
	 * Extracted name from {@link #getMethodSelect()}.
	 *
	 * @return Method name.
	 */
	@Nonnull
	public String getMethodName() {
		if (methodSelect instanceof NameExpressionModel nameExpr)
			return nameExpr.getName();
		else if (methodSelect instanceof MemberSelectExpressionModel selectExpr)
			return selectExpr.getName();
		throw new IllegalStateException("Unsupported method-select element: " + methodSelect.getClass().getSimpleName());
	}

	/**
	 * Method invocation receiver, IE the {@code context} in {@code context.method()}.
	 *
	 * @return Method receiver.
	 */
	@Nullable
	public Model getReceiver() {
		if (methodSelect instanceof MemberSelectExpressionModel selectExpr)
			return selectExpr.getContext();
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MethodInvocationExpressionModel that = (MethodInvocationExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!typeArguments.equals(that.typeArguments)) return false;
		if (!methodSelect.equals(that.methodSelect)) return false;
		return arguments.equals(that.arguments);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + typeArguments.hashCode();
		result = 31 * result + methodSelect.hashCode();
		result = 31 * result + arguments.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (typeArguments != null && !typeArguments.isEmpty())
			sb.append('<').append(typeArguments.stream().map(Object::toString).collect(Collectors.joining(", "))).append('>');
		sb.append(methodSelect.toString());
		sb.append('(').append(arguments.stream().map(Object::toString).collect(Collectors.joining(", "))).append(')');
		return sb.toString();
	}
}
