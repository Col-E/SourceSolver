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
	public String toString() {
		return "import " + name;
	}
}
