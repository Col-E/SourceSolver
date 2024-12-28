package software.coley.sourcesolver.util;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Simple range model.
 *
 * @param begin
 * 		Start of range.
 * @param end
 * 		End of range.
 *
 * @author Matt Coley
 */
public record Range(int begin, int end) implements Comparable<Range> {
	public static final Range UNKNOWN = new Range(-1, -1);

	/**
	 * @param table
	 * 		Table to lookup tree positions within.
	 * @param tree
	 * 		Tree to calculate range of.
	 *
	 * @return Range covering tree.
	 */
	@Nonnull
	public static Range extractRange(@Nonnull EndPosTable table, @Nonnull Tree tree) {
		if (tree instanceof JCTree treeImpl) {
			JCDiagnostic.DiagnosticPosition pos = treeImpl.pos();
			return new Range(pos.getStartPosition(), pos.getEndPosition(table));
		}
		throw new IllegalArgumentException("Cannot resolve range of unexpected tree type: "
				+ tree.getClass().getName());
	}

	/**
	 * @param table
	 * 		Table to lookup tree positions within.
	 * @param trees
	 * 		Trees to calculate range of.
	 *
	 * @return Range covering all trees.
	 */
	@Nonnull
	public static Range extractRange(@Nonnull EndPosTable table, @Nonnull Collection<? extends Tree> trees) {
		if (trees.isEmpty())
			throw new IllegalArgumentException("Cannot extract range of empty tree collection");

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (Tree tree : trees) {
			Range range = extractRange(table, tree);
			if (range.begin < min)
				min = range.begin;
			int end = range.end();
			if (end > max)
				max = end;
		}

		return new Range(min, max);
	}

	/**
	 * @param position
	 * 		Position to check.
	 *
	 * @return {@code true} when the position is in the inclusive range.
	 */
	public boolean isWithin(int position) {
		return isWithin(position, true, true);
	}

	/**
	 * @param position
	 * 		Position to check.
	 * @param startInclusive
	 *        {@code true} for an inclusive lower bound.
	 * @param endInclusive
	 *        {@code true} for an inclusive upper bound.
	 *
	 * @return {@code true} when the position is in the range.
	 */
	public boolean isWithin(int position, boolean startInclusive, boolean endInclusive) {
		if (startInclusive ? position < begin : position <= begin)
			return false;
		int end = end();
		return endInclusive ? position <= end : position < end;
	}

	/**
	 * @return {@code true} when the range has bogus values.
	 */
	public boolean isUnknown() {
		return begin < 0 || end < 0;
	}

	@Override
	public int compareTo(Range o) {
		int cmp = Integer.compare(begin, o.begin);
		if (cmp == 0)
			cmp = -Integer.compare(end, o.end);
		return cmp;
	}
}
