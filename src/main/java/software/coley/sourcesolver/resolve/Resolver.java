package software.coley.sourcesolver.resolve;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.model.ClassModel;
import software.coley.sourcesolver.model.CompilationUnitModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.resolve.result.Resolutions;

import java.util.List;

/**
 * Outlines resolving capabilities.
 *
 * @author Matt Coley
 */
public interface Resolver {
	/**
	 * Attempt to resolve the item at the given position.
	 *
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
	 * Attempt to resolve the item at the given position, with an optional target model to resolve.
	 *
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
	 * Attempt to resolve a simple reference at the given position.
	 * <br>
	 * This is intended for cases where tooling has a source fragment, but not an exact mapped AST model.
	 * Implementations should resolve the name as it would be seen from the provided source position, such as:
	 * <ul>
	 *     <li>Local variables and parameters</li>
	 *     <li>Fields visible from the enclosing class</li>
	 *     <li>Type parameters</li>
	 *     <li>Visible types by simple or qualified name</li>
	 * </ul>
	 *
	 * @param name
	 * 		Reference text to resolve.
	 * @param position
	 * 		Absolute position in the source code near where the reference is used.
	 *
	 * @return Resolution of the reference, or {@link Resolutions#unknown()} when it cannot be resolved.
	 */
	@Nonnull
	default Resolution resolveReferenceAt(@Nonnull String name, int position) {
		return Resolutions.unknown();
	}

	/**
	 * Attempt to resolve a source fragment at the given position.
	 * <br>
	 * This is intended for cases where tooling has source text for a value-producing fragment, but not an exact
	 * mapped AST model. Implementations may resolve the fragment by matching existing models near the position or by
	 * evaluating simple chained member access patterns.
	 *
	 * @param text
	 * 		Source fragment text to resolve.
	 * @param position
	 * 		Absolute position in the source code near where the fragment is used.
	 *
	 * @return Resolution of the fragment, or {@link Resolutions#unknown()} when it cannot be resolved.
	 */
	@Nonnull
	default Resolution resolveFragmentAt(@Nonnull String text, int position) {
		return Resolutions.unknown();
	}

	/**
	 * Attempt to resolve a field in the context of an already-resolved receiver.
	 *
	 * @param contextResolution
	 * 		Resolution of the receiver or owner type.
	 * @param fieldName
	 * 		Field name to resolve.
	 *
	 * @return Field resolution, or {@link Resolutions#unknown()} when the field cannot be resolved.
	 */
	@Nonnull
	default Resolution resolveFieldInContext(@Nonnull Resolution contextResolution, @Nonnull String fieldName) {
		return resolveFieldInContext(contextResolution, fieldName, null);
	}

	/**
	 * Attempt to resolve a field in the context of an already-resolved receiver.
	 *
	 * @param contextResolution
	 * 		Resolution of the receiver or owner type.
	 * @param fieldName
	 * 		Field name to resolve.
	 * @param typeHint
	 * 		Optional expected field type hint.
	 *
	 * @return Field resolution, or {@link Resolutions#unknown()} when the field cannot be resolved.
	 */
	@Nonnull
	default Resolution resolveFieldInContext(@Nonnull Resolution contextResolution,
	                                         @Nonnull String fieldName,
	                                         @Nullable DescribableEntry typeHint) {
		return Resolutions.unknown();
	}

	/**
	 * Attempt to resolve a method in the context of an already-resolved receiver.
	 *
	 * @param contextResolution
	 * 		Resolution of the receiver or owner type.
	 * @param methodName
	 * 		Method name to resolve.
	 *
	 * @return Method resolution, or {@link Resolutions#unknown()} when the method cannot be resolved.
	 */
	@Nonnull
	default Resolution resolveMethodInContext(@Nonnull Resolution contextResolution, @Nonnull String methodName) {
		return resolveMethodInContext(contextResolution, methodName, null, null);
	}

	/**
	 * Attempt to resolve a method in the context of an already-resolved receiver.
	 *
	 * @param contextResolution
	 * 		Resolution of the receiver or owner type.
	 * @param methodName
	 * 		Method name to resolve.
	 * @param returnTypeHint
	 * 		Optional expected return type hint.
	 * @param argumentTypeHints
	 * 		Optional argument type hints.
	 *
	 * @return Method resolution, or {@link Resolutions#unknown()} when the method cannot be resolved.
	 */
	@Nonnull
	default Resolution resolveMethodInContext(@Nonnull Resolution contextResolution,
	                                          @Nonnull String methodName,
	                                          @Nullable DescribableEntry returnTypeHint,
	                                          @Nullable List<? extends DescribableEntry> argumentTypeHints) {
		return Resolutions.unknown();
	}

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
