package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.PackageTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AnnotationUseModel;
import software.coley.sourcesolver.model.ClassModel;
import software.coley.sourcesolver.model.CompilationUnitModel;
import software.coley.sourcesolver.model.ImportModel;
import software.coley.sourcesolver.model.PackageModel;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.util.Range.extractRange;

public class CompilationUnitMapper implements Mapper<CompilationUnitModel, CompilationUnitTree> {
	@Nonnull
	@Override
	public CompilationUnitModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull CompilationUnitTree tree) {
		// Package
		List<AnnotationUseModel> packageAnnotations;
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
		context.setMapperSupplier(ClassMapper.class, () -> new ClassMapper(packageModel, importModels));
		List<ClassModel> classModels = tree.getTypeDecls().stream()
				.filter(t -> t instanceof ClassTree)
				.map(t -> (ClassTree) t)
				.map(ct -> context.map(ClassMapper.class, ct))
				.toList();
		return new CompilationUnitModel(extractRange(table, tree), classModels);
	}
}
