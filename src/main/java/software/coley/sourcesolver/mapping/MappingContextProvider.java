package software.coley.sourcesolver.mapping;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.Parser;
import software.coley.sourcesolver.util.RangeExtractor;

/**
 * Supplies a {@link MappingContext}.
 *
 * @author Matt Coley
 * @see Parser#setMappingContextFactory(MappingContextProvider)
 */
public interface MappingContextProvider {
	/**
	 * @param extractor
	 * 		Range extractor to use for calculating source code ranges of trees.
	 * @param source
	 * 		Java source code.
	 *
	 * @return New mapping context.
	 */
	@Nonnull
	MappingContext newMappingContext(@Nonnull RangeExtractor extractor, @Nonnull String source);
}
