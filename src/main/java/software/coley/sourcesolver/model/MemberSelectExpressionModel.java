package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class MemberSelectExpressionModel extends AbstractExpressionModel implements NamedModel {
	private final String name;
	private final Model context;

	public MemberSelectExpressionModel(@Nonnull Range range, @Nonnull String name, @Nonnull Model context) {
		super(range, context);
		this.name = name;
		this.context = context;
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nonnull
	public Model getContext() {
		return context;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MemberSelectExpressionModel that = (MemberSelectExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!name.equals(that.name)) return false;
		return context.equals(that.context);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + context.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return getContext().toString() + '.' + name;
	}
}
