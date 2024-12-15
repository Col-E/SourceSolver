package software.coley.sourcesolver.mapping;

import com.sun.tools.javac.tree.EndPosTable;

import javax.annotation.Nonnull;

public interface MappingContextProvider {
	@Nonnull
	MappingContext newMappingContext(@Nonnull EndPosTable table, @Nonnull String source);
}
