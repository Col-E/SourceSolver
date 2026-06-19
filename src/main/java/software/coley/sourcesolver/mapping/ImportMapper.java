package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ImportTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.ImportModel;

public class ImportMapper implements Mapper<ImportModel, ImportTree> {
	@Nonnull
	@Override
	public ImportModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull ImportTree tree) {
		return new ImportModel(extractor.get(tree), tree.isStatic(), tree.getQualifiedIdentifier().toString());
	}
}
