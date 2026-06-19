package software.coley.sourcesolver.mapping;

import com.sun.source.tree.Tree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.Model;

/**
 * Outlines the conversion of a javac tree element to our own model alternative.
 *
 * @param <M>
 * 		Model type to output.
 * @param <T>
 * 		Tree type to convert.
 *
 * @author Matt Coley
 */
public interface Mapper<M extends Model, T extends Tree> {
	/**
	 * Maps the given tree into our own model format.
	 *
	 * @param context
	 * 		Mapping context to do additional work within.
	 * @param extractor
	 * 		Extractor to use for calculating source code ranges of the tree.
	 * @param tree
	 * 		Tree to map.
	 *
	 * @return Model representation of the tree.
	 */
	@Nonnull
	M map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull T tree);
}
