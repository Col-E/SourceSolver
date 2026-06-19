package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.PackageTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.ClassModel;
import software.coley.sourcesolver.model.CompilationUnitModel;
import software.coley.sourcesolver.model.ImportModel;
import software.coley.sourcesolver.model.PackageModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CompilationUnitMapper implements Mapper<CompilationUnitModel, CompilationUnitTree> {
	private final String inputSource;

	public CompilationUnitMapper(@Nonnull String inputSource) {
		this.inputSource = inputSource;
	}

	@Nonnull
	@Override
	public CompilationUnitModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull CompilationUnitTree tree) {
		// Package
		List<AnnotationExpressionModel> packageAnnotations;
		if (tree.getPackageAnnotations() == null)
			packageAnnotations = Collections.emptyList();
		else {
			packageAnnotations = tree.getPackageAnnotations().stream()
					.map(anno -> context.map(AnnotationUseMapper.class, anno))
					.collect(Collectors.toList());
		}
		context.setMapperSupplier(PackageMapper.class, () -> new PackageMapper(packageAnnotations));
		PackageTree packageDeclaration = tree.getPackage();
		PackageModel packageModel = packageDeclaration == null ? PackageModel.DEFAULT_PACKAGE : context.map(PackageMapper.class, packageDeclaration);

		// Imports
		List<ImportModel> importModels = tree.getImports().stream()
				.map(i -> context.map(ImportMapper.class, i))
				.toList();

		// Class declarations
		List<ClassModel> classModels = tree.getTypeDecls().stream()
				.filter(t -> t instanceof ClassTree)
				.map(t -> (ClassTree) t)
				.map(ct -> context.map(ClassMapper.class, ct))
				.toList();
		return new CompilationUnitModel(extractor.get(tree), inputSource, packageModel, importModels, classModels);
	}
}
