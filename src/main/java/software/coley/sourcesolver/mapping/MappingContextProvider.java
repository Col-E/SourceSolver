package software.coley.sourcesolver.mapping;

import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.Parser;

import jakarta.annotation.Nonnull;

/**
 * Supplies a {@link MappingContext}.
 *
 * @author Matt Coley
 * @see Parser#setMappingContextFactory(MappingContextProvider)
 */
public interface MappingContextProvider {
	/**
	 * @param table
	 * 		Table to lookup tree positions within.
	 * @param source
	 * 		Java source code.
	 *
	 * @return New mapping context.
	 */
	@Nonnull
	MappingContext newMappingContext(@Nonnull EndPosTable table, @Nonnull String source);
}
