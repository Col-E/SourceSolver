package software.coley.sourcesolver.mapping;

import com.sun.source.tree.PackageTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.model.PackageModel;

import java.util.List;

public class PackageMapper implements Mapper<PackageModel, PackageTree> {
	private final List<AnnotationExpressionModel> packageAnnotations;

	public PackageMapper(@Nonnull List<AnnotationExpressionModel> packageAnnotations) {
		this.packageAnnotations = packageAnnotations;
	}

	@Nonnull
	@Override
	public PackageModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull PackageTree tree) {
		NameExpressionModel name = context.map(NameMapper.class, tree.getPackageName());
		return new PackageModel(extractor.get(tree), name, packageAnnotations);
	}
}
