package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NewClassExpressionModel extends AbstractExpressionModel implements NamedModel {
	private final AbstractExpressionModel enclosingExpression;
	private final List<Model> typeArguments;
	private final AbstractExpressionModel identifier;
	private final List<AbstractExpressionModel> arguments;
	private final ClassModel body;

	public NewClassExpressionModel(@Nonnull Range range,
	                               @Nullable AbstractExpressionModel enclosingExpression,
	                               @Nonnull List<Model> typeArguments,
	                               @Nonnull AbstractExpressionModel identifier,
	                               @Nonnull List<AbstractExpressionModel> arguments,
	                               @Nullable ClassModel body) {
		super(range);
		this.enclosingExpression = enclosingExpression;
		this.typeArguments = typeArguments;
		this.identifier = identifier;
		this.arguments = arguments;
		this.body = body;
	}

	@Nullable
	public AbstractExpressionModel getEnclosingExpression() {
		return enclosingExpression;
	}

	@Nonnull
	public List<Model> getTypeArguments() {
		return typeArguments;
	}

	@Nonnull
	public AbstractExpressionModel getIdentifier() {
		return identifier;
	}

	@Nonnull
	public List<AbstractExpressionModel> getArguments() {
		return arguments;
	}

	@Nullable
	public ClassModel getBody() {
		return body;
	}

	@Nonnull
	@Override
	public String getName() {
		return getIdentifier() instanceof NamedModel named ? named.getName() : getIdentifier().toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NewClassExpressionModel that = (NewClassExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!Objects.equals(enclosingExpression, that.enclosingExpression))
			return false;
		if (!typeArguments.equals(that.typeArguments)) return false;
		if (!identifier.equals(that.identifier)) return false;
		if (!arguments.equals(that.arguments)) return false;
		return Objects.equals(body, that.body);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + (enclosingExpression != null ? enclosingExpression.hashCode() : 0);
		result = 31 * result + typeArguments.hashCode();
		result = 31 * result + identifier.hashCode();
		result = 31 * result + arguments.hashCode();
		result = 31 * result + (body != null ? body.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (enclosingExpression != null)
			sb.append(enclosingExpression).append('.');
		sb.append("new ").append(identifier);
		if (typeArguments != null && !typeArguments.isEmpty())
			sb.append('<').append(typeArguments.stream().map(Object::toString).collect(Collectors.joining(", "))).append('>');
		sb.append('(').append(arguments.stream().map(Object::toString).collect(Collectors.joining(", "))).append(')');
		if (body != null)
			sb.append(body);
		return sb.toString();
	}
}
