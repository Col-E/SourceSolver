package software.coley.sourcesolver.model;

import software.coley.sourcesolver.resolve.Resolver;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

	@Nullable
	default Model getChildAtPosition(int position) {
		for (Model child : getChildren())
			if (child.getRange().isWithin(position))
				return child;
		return null;
	}

	@Nullable
	default Model getParentOfType(@Nonnull Class<? extends Model> type) {
		if (getClass().isAssignableFrom(type))
			return this;
		Model parent = getParent();
		if (parent != null)
			return parent.getParentOfType(type);
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
