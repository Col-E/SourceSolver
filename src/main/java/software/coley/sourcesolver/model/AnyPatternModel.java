package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

/**
 * Model switch pattern matching as described in the JDK 21 preview feature implementation.
 * <br>
 * See: <a href="https://github.com/openjdk/jdk21u/blob/master/src/jdk.compiler/share/classes/com/sun/source/tree/AnyPatternTree.java">{@code ParenthesizedPatternTree}</a>
 *
 * @author Matt Coley
 */
@SuppressWarnings("unused")
public class AnyPatternModel extends AbstractPatternModel {
	public AnyPatternModel(@Nonnull Range range) {
		super(range);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof AnyPatternModel;
	}

	@Override
	public int hashCode() {
		return 2345;
	}

	@Override
	public String toString() {
		return "*";
	}
}
