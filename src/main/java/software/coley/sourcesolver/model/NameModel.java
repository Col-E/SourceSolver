package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class NameModel extends AbstractModel {
	private final String name;

	public NameModel(@Nonnull Range range, @Nonnull String name) {
		super(range);
		this.name = name;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		NameModel nameModel = (NameModel) o;

		return name.equals(nameModel.name);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return name;
	}
}
