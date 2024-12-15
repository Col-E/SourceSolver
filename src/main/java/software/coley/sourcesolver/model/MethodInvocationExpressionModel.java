package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class MethodInvocationExpressionModel extends AbstractExpressionModel {
	private final List<AbstractModel> typeArguments;
	private final AbstractExpressionModel methodSelect;
	private final List<AbstractExpressionModel> arguments;

	public MethodInvocationExpressionModel(@Nonnull Range range, @Nonnull List<AbstractModel> typeArguments,
	                                       @Nonnull AbstractExpressionModel methodSelect, @Nonnull List<AbstractExpressionModel> arguments) {
		super(range);
		this.typeArguments = typeArguments;
		this.methodSelect = methodSelect;
		this.arguments = arguments;
	}

	@Nonnull
	public List<AbstractModel> getTypeArguments() {
		return typeArguments;
	}

	@Nonnull
	public AbstractExpressionModel getMethodSelect() {
		return methodSelect;
	}

	@Nonnull
	public List<AbstractExpressionModel> getArguments() {
		return arguments;
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
