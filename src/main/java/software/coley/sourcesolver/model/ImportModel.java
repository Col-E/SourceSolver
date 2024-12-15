package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class ImportModel extends AbstractModel {
	private final String name;
	private final boolean isStatic;

	public ImportModel(@Nonnull Range range, boolean isStatic, @Nonnull String name) {
		super(range);
		this.isStatic = isStatic;
		this.name = name;
	}

	public boolean isStatic() {
		return isStatic;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ImportModel that = (ImportModel) o;

		if (isStatic != that.isStatic) return false;
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + (isStatic ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return "import " + name;
	}
}
