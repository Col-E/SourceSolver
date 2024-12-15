package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class TypeParameterModel extends AbstractModel {
	public TypeParameterModel(@Nonnull Range range) {
		super(range);
	}

	@Override
	public boolean equals(Object o) {
		// TODO: Flesh out this class
		return o == this;
	}

	@Override
	public int hashCode() {
		return getRange().hashCode();
	}
}
