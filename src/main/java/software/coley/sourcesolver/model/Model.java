package software.coley.sourcesolver.model;

import software.coley.sourcesolver.resolve.Resolver;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Base model type for all AST elements.
 *
 * @author Matt Coley
 */
public interface Model {
	/**
	 * @param resolver
	 * 		Resolver to utilize.
	 *
	 * @return Resolution of what this model represents.
	 */
	@Nonnull
	default Resolution resolve(@Nonnull Resolver resolver) {
		Range range = getRange();
		if (range.isUnknown())
			return resolveAt(resolver, -1);

		Model parent = getParent();
		int index = range.begin();
		if (index < 0 && parent != null)
			index = parent.getRange().begin();
		return resolveAt(resolver, index);
	}

	/**
	 * @param resolver
	 * 		Resolver to utilize.
	 * @param position
	 * 		Absolute position in the source code of the item we want to resolve.
	 *
	 * @return Resolution of what this model represents at the given position.
	 */
	@Nonnull
	Resolution resolveAt(@Nonnull Resolver resolver, int position);

	/**
	 * Visits the current model and all children with the given visitor.
	 *
	 * @param visitor
	 * 		Visitor to observe this model and all children.
	 */
	default void visit(@Nonnull ModelVisitor visitor) {
		if (visitor.visit(this))
			for (Model child : getChildren())
				child.visit(visitor);
	}

	/**
	 * @param position
	 * 		Absolute position in the source code.
	 *
	 * @return Child contained by this model that contains the given position.
	 * {@code null} if no child contains the given point.
	 */
	@Nullable
	default Model getChildAtPosition(int position) {
		for (Model child : getChildren())
			if (child.getRange().isWithin(position))
				return child;
		return null;
	}

	/**
	 * @param type
	 * 		Model type to get.
	 * @param <M>
	 * 		Model type.
	 *
	 * @return List of all children at all depths that are assignable to the requested type.
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	default <M extends Model> List<M> getRecursiveChildrenOfType(@Nonnull Class<M> type) {
		List<M> models = new ArrayList<>();
		visit(child -> {
			if (type.isAssignableFrom(child.getClass()))
				models.add((M) child);
			return true;
		});
		return models;
	}

	/**
	 * @param type
	 * 		Model type to get.
	 * @param <M>
	 * 		Model type.
	 *
	 * @return The first parent in this model's hierarchy that is assignable to the requested type.
	 * {@code null} if no parent in the model matches.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	default <M extends Model> M getParentOfType(@Nonnull Class<M> type) {
		Model parent = getParent();
		while (parent != null) {
			if (type.isAssignableFrom(parent.getClass()))
				return (M) parent;
			parent = parent.getParent();
		}
		return null;
	}

	/**
	 * @param unit
	 * 		Unit holding the original source code.
	 *
	 * @return Substring of source code this model is backed by.
	 */
	@Nonnull
	default String getSource(@Nonnull CompilationUnitModel unit) {
		Range range = getRange();
		if (range.isUnknown()) return "";

		String src = unit.getInputSource();
		int begin = Math.max(0, range.begin());
		int end = Math.min(src.length(), range.end());
		return src.substring(begin, end);
	}

	/**
	 * @return Direct child models.
	 */
	@Nonnull
	List<Model> getChildren();

	/**
	 * @return Direct parent model. Should only ever be {@code null} for {@link CompilationUnitModel}.
	 */
	@Nullable
	Model getParent();

	/**
	 * @return Range within the source code this model originates from.
	 */
	@Nonnull
	Range getRange();
}
