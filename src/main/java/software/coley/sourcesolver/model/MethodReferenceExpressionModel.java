package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class MethodReferenceExpressionModel extends AbstractExpressionModel {
	private final AbstractExpressionModel qualifier;
	private final NameExpressionModel name;
	private final List<Model> typeArguments;
	private final Mode mode;

	public MethodReferenceExpressionModel(@Nonnull Range range,
	                                      @Nonnull Mode mode,
	                                      @Nonnull AbstractExpressionModel qualifier,
	                                      @Nonnull NameExpressionModel name,
	                                      @Nonnull List<Model> typeArguments) {
		super(range, of(qualifier), of(name), of(typeArguments));
		this.mode = mode;
		this.qualifier = qualifier;
		this.name = name;
		this.typeArguments = typeArguments;
	}

	@Nonnull
	public Mode getMode() {
		return mode;
	}

	@Nonnull
	public AbstractExpressionModel getQualifier() {
		return qualifier;
	}

	@Nonnull
	public NameExpressionModel getName() {
		return name;
	}

	@Nonnull
	public List<Model> getTypeArguments() {
		return typeArguments;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MethodReferenceExpressionModel that = (MethodReferenceExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!mode.equals(that.mode)) return false;
		if (!typeArguments.equals(that.typeArguments)) return false;
		if (!qualifier.equals(that.qualifier)) return false;
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + mode.hashCode();
		result = 31 * result + typeArguments.hashCode();
		result = 31 * result + qualifier.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

	@Override
	public String toString() {
		// TODO: Fix this to differentiate between modes
		StringBuilder sb = new StringBuilder();
		if (typeArguments != null && !typeArguments.isEmpty())
			sb.append('<').append(typeArguments.stream().map(Object::toString).collect(Collectors.joining(", "))).append('>');
		sb.append(qualifier).append('.').append(name);
		return sb.toString();
	}

	public enum Mode {
		INVOKE, NEW
	}
}
