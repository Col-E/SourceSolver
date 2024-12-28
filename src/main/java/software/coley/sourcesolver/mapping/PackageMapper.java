package software.coley.sourcesolver.mapping;

import com.sun.source.tree.PackageTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.model.PackageModel;

import jakarta.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class PackageMapper implements Mapper<PackageModel, PackageTree> {
	private final List<AnnotationExpressionModel> packageAnnotations;

	public PackageMapper(@Nonnull List<AnnotationExpressionModel> packageAnnotations) {
		this.packageAnnotations = packageAnnotations;
	}

	@Nonnull
	@Override
	public PackageModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull PackageTree tree) {
		NameExpressionModel name = context.map(NameMapper.class, tree.getPackageName());
		return new PackageModel(extractRange(table, tree), name, packageAnnotations);
	}
}
