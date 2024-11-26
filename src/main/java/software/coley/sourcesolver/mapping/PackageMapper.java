package software.coley.sourcesolver.mapping;

import com.sun.source.tree.PackageTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AnnotationUseModel;
import software.coley.sourcesolver.model.NameModel;
import software.coley.sourcesolver.model.PackageModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class PackageMapper {
	private final List<AnnotationUseModel> packageAnnotations;

	public PackageMapper(@Nonnull List<AnnotationUseModel> packageAnnotations) {
		this.packageAnnotations = packageAnnotations;
	}

	@Nonnull
	public PackageModel map(@Nonnull EndPosTable table, @Nullable PackageTree tree) {
		if (tree == null)
			return PackageModel.DEFAULT_PACKAGE;

		NameModel name = new NameMapper().map(table, tree.getPackageName());
		return new PackageModel(extractRange(table, tree), name, packageAnnotations);
	}
}
