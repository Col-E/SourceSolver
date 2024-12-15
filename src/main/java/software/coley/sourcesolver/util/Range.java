package software.coley.sourcesolver.util;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic;

import javax.annotation.Nonnull;
import java.util.Collection;

public record Range(int begin, int end) implements Comparable<Range> {
	public static final Range UNKNOWN = new Range(-1, -1);

	@Nonnull
	public static Range extractRange(@Nonnull EndPosTable table, @Nonnull Tree tree) {
		if (tree instanceof JCTree treeImpl) {
			JCDiagnostic.DiagnosticPosition pos = treeImpl.pos();
			return new Range(pos.getStartPosition(), pos.getEndPosition(table));
		}
		throw new IllegalArgumentException("Cannot resolve range of unexpected tree type: "
				+ tree.getClass().getName());
	}

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

		return new Range(min, (max - min));
	}

	public boolean isWithin(int position) {
		return isWithin(position, true, true);
	}

	public boolean isWithin(int position, boolean startInclusive, boolean endInclusive) {
		if (startInclusive ? position < begin : position <= begin)
			return false;
		int end = end();
		return endInclusive ? position <= end : position < end;
	}

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
