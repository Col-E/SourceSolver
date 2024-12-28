package software.coley.sourcesolver.resolve;

import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.resolve.result.Resolution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Outlines resolving capabilities.
 *
 * @author Matt Coley
 */
public interface Resolver {
	/**
	 * @param position
	 * 		Absolute position in the source code of the item we want to resolve.
	 *
	 * @return Resolution of what the deepest nested model at the given position represents.
	 */
	@Nonnull
	default Resolution resolveAt(int position) {
		return resolveAt(position, null);
	}

	/**
	 * @param position
	 * 		Absolute position in the source code of the item we want to resolve.
	 * @param target
	 * 		The target model to resolve. Can be {@code null} to auto-pick a model at the given position.
	 *
	 * @return Resolution of what the given target model represents.
	 */
	@Nonnull
	Resolution resolveAt(int position, @Nullable Model target);
}
