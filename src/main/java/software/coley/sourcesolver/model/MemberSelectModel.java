package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class MemberSelectModel extends AbstractModel {
	private final String name;
	private final AbstractModel context;

	public MemberSelectModel(@Nonnull Range range, @Nonnull String name, @Nonnull AbstractModel context) {
		super(range, context);
		this.name = name;
		this.context = context;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Nonnull
	public AbstractModel getContext() {
		return context;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		MemberSelectModel that = (MemberSelectModel) o;

		if (!name.equals(that.name)) return false;
		return context.equals(that.context);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + context.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return getContext().toString() + '.' + name;
	}
}
