package software.coley.sourcesolver.util.compat;

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.tree.JCTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.Range;
import software.coley.sourcesolver.util.RangeExtractor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * <a href="https://github.com/openjdk/jdk/commit/b0829a54cd787d5e378573f69ec0b82b40602454.patch">"Existence is pain" - Java 27</a>
 * <p>
 * This is the post-Java 27 implementation of the range factory.
 */
public class RangeExtractorFactory27 {
	private static final MethodHandle END_HANDLE;
	static {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			END_HANDLE = lookup.findVirtual(
					JCTree.class,
					"getEndPosition",
					MethodType.methodType(int.class)
			);
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException("Failed to access JCTree#getEndPosition()", ex);
		}
	}

	/**
	 * @param parser
	 * 		Context to extract from.
	 *
	 * @return Range extractor for the given context.
	 */
	public static @Nonnull RangeExtractor get(@Nonnull JavacParser parser) {
		return tree -> {
			try {
				if (tree instanceof JCTree treeImpl) {
					int start = treeImpl.getStartPosition();
					int end = (int) END_HANDLE.invokeExact(treeImpl);
					return new Range(start, end);
				}
			} catch (Throwable t) {
				throw new RuntimeException("Failed to extract range from tree: " + tree.getClass().getName(), t);
			}
			throw new IllegalStateException("Cannot resolve range of unexpected tree type: " + tree.getClass().getName());
		};
	}
}
