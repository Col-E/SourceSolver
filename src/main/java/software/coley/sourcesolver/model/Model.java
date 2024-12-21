package software.coley.sourcesolver.model;

import software.coley.sourcesolver.resolve.Resolver;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public interface Model {
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

	@Nonnull
	Resolution resolveAt(@Nonnull Resolver resolver, int position);

	default void visit(@Nonnull ModelVisitor visitor) {
		if (visitor.visit(this))
			for (Model child : getChildren())
				child.visit(visitor);
	}

	@Nullable
	default Model getChildAtPosition(int position) {
		for (Model child : getChildren())
			if (child.getRange().isWithin(position))
				return child;
		return null;
	}

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

	@Nonnull
	default String getSource(@Nonnull CompilationUnitModel unit) {
		Range range = getRange();
		if (range.isUnknown()) return "";

		String src = unit.getInputSource();
		int begin = Math.max(0, range.begin());
		int end = Math.min(src.length(), range.end());
		return src.substring(begin, end);
	}

	@Nonnull
	List<Model> getChildren();

	@Nullable
	Model getParent();

	@Nonnull
	Range getRange();
}
