package software.coley.sourcesolver.mapping;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.Model;

import jakarta.annotation.Nonnull;

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
	 * @param table
	 * 		Table to lookup tree positions within.
	 * @param tree
	 * 		Tree to map.
	 *
	 * @return Model representation of the tree.
	 */
	@Nonnull
	M map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull T tree);
}
