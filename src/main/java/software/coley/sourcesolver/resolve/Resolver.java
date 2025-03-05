package software.coley.sourcesolver.resolve;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.model.ClassModel;
import software.coley.sourcesolver.model.CompilationUnitModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.result.Resolution;

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

	/**
	 * Tell the resolver to trust that a given class model in the {@link CompilationUnitModel} should be resolved
	 * to the given class entry. This can be used in situations where the name of the {@link ClassModel} does not
	 * reflect the exact contents of what is defined by the {@link ClassEntry}. This can be useful when resolving
	 * decompiled code and the class is an isolated inner class, which normally would be difficult/impossible to
	 * infer from just the provided source.
	 *
	 * @param declaredClassModel
	 * 		Model to resolve.
	 * @param declaredClassEntry
	 * 		Resolution target to associate with the model.
	 */
	void setDeclaredClass(@Nonnull ClassModel declaredClassModel, @Nullable ClassEntry declaredClassEntry);
}
