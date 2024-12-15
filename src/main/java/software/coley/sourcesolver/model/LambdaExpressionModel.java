package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class LambdaExpressionModel extends AbstractExpressionModel {
	private final List<VariableModel> parameters;
	private final AbstractModel body;
	private final BodyKind bodyKind;

	public LambdaExpressionModel(@Nonnull Range range,
	                             @Nonnull List<VariableModel> parameters,
	                             @Nonnull AbstractModel body,
	                             @Nonnull BodyKind bodyKind) {
		super(range, of(parameters), of(body));
		this.parameters = parameters;
		this.body = body;
		this.bodyKind = bodyKind;
	}

	@Nonnull
	public List<VariableModel> getParameters() {
		return parameters;
	}

	@Nonnull
	public AbstractModel getBody() {
		return body;
	}

	@Nonnull
	public BodyKind getBodyKind() {
		return bodyKind;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LambdaExpressionModel that = (LambdaExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!parameters.equals(that.parameters)) return false;
		if (!body.equals(that.body)) return false;
		return bodyKind == that.bodyKind;
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + parameters.hashCode();
		result = 31 * result + body.hashCode();
		result = 31 * result + bodyKind.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(').append(parameters.stream().map(Object::toString).collect(Collectors.joining(", "))).append(')');
		sb.append(" -> ");
		switch (bodyKind) {
			case EXPRESSION -> sb.append(body);
			case STATEMENT -> sb.append('{').append(body).append('}');
		}
		return sb.toString();
	}

	public enum BodyKind {
		EXPRESSION, STATEMENT
	}
}
