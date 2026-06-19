package software.coley.sourcesolver.util;

import com.sun.source.tree.Tree;
import jakarta.annotation.Nonnull;

import java.util.Collection;

/**
 * Extracts source code ranges from trees.
 *
 * @author Matt Coley
 */
public interface RangeExtractor {
	/**
	 * @param trees
	 * 		Collection of trees to extract a range from.
	 *
	 * @return Range covering all given trees.
	 */
	@Nonnull
	default Range get(@Nonnull Collection<? extends Tree> trees) {
		if (trees.isEmpty())
			throw new IllegalArgumentException("Cannot extract range of empty tree collection");

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (Tree tree : trees) {
			Range range = get(tree);
			if (range.begin() < min)
				min = range.begin();
			int end = range.end();
			if (end > max)
				max = end;
		}

		return new Range(min, max);
	}

	/**
	 * @param tree
	 * 		Tree to extract range of.
	 *
	 * @return Range covering the given tree.
	 */
	@Nonnull
	Range get(@Nonnull Tree tree);
}
