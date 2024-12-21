package software.coley.sourcesolver.resolve;

import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.resolve.result.Resolution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Resolver {
	@Nonnull
	default Resolution resolveAt(int index) {
		return resolveAt(index, null);
	}

	@Nonnull
	Resolution resolveAt(int index, @Nullable Model target);
}
