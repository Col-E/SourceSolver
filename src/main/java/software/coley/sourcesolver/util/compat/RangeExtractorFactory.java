package software.coley.sourcesolver.util.compat;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.Range;
import software.coley.sourcesolver.util.RangeExtractor;

import java.lang.reflect.Field;

/**
 * <a href="https://github.com/openjdk/jdk/commit/b0829a54cd787d5e378573f69ec0b82b40602454.patch">"Existence is pain" - Java 27</a>
 * <p>
 * This is the pre-Java 27 implementation of the range factory.
 */
public class RangeExtractorFactory {
	/**
	 * @param parser
	 * 		Context to extract from.
	 *
	 * @return Range extractor for the given context.
	 */
	public static @Nonnull RangeExtractor get(@Nonnull JavacParser parser) {
		try {
			EndPosTable table = extractEndPosTable(parser);
			return tree -> extractRange(table, tree);
		} catch (Throwable t) {
			throw new IllegalStateException("Failed to extract end-pos table (pre Java 27) from javac parser", t);
		}
	}

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
	 * Retrieves the private table information we use for mapping our model's ranges.
	 *
	 * @param parser
	 * 		Context to reflect from.
	 *
	 * @return End position table within the given parser.
	 *
	 * @throws Exception
	 * 		When the table cannot be reflected.
	 */
	@Nonnull
	public static EndPosTable extractEndPosTable(@Nonnull JavacParser parser) throws Exception {
		Field field = JavacParser.class.getDeclaredField("endPosTable");
		field.setAccessible(true);
		return (EndPosTable) field.get(parser);
	}
}
