package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ImportTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.ImportModel;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.util.Range.extractRange;

public class ImportMapper implements Mapper<ImportModel, ImportTree> {
	@Nonnull
	@Override
	public ImportModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull ImportTree tree) {
		return new ImportModel(extractRange(table, tree), tree.isStatic(), tree.getQualifiedIdentifier().toString());
	}
}
