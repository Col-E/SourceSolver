package software.coley.sourcesolver.mapping;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;

import javax.annotation.Nonnull;

public interface Mapper<M extends AbstractModel, T extends Tree> {
	@Nonnull
	M map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull T tree);
}
