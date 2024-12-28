package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class DefaultCaseLabelModel extends AbstractCaseLabelModel {
	public DefaultCaseLabelModel(@Nonnull Range range) {
		super(range);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof DefaultCaseLabelModel;
	}

	@Override
	public int hashCode() {
		return 1234567;
	}

	@Override
	public String toString() {
		return "default";
	}
}
