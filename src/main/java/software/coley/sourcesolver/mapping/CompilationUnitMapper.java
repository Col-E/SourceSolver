package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
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

public class CompilationUnitMapper {
	@Nonnull
	public CompilationUnitModel map(@Nonnull EndPosTable table, @Nonnull CompilationUnitTree tree) {
		// Package
		List<AnnotationUseModel> packageAnnotations;
		if (tree.getPackageAnnotations() == null)
			packageAnnotations = Collections.emptyList();
		else {
			AnnotationUseMapper annotationMapper = new AnnotationUseMapper();
			packageAnnotations = tree.getPackageAnnotations().stream()
					.map(anno -> annotationMapper.map(table, anno))
					.collect(Collectors.toList());
		}
		PackageModel packageModel = new PackageMapper(packageAnnotations).map(table, tree.getPackage());

		// Imports
		ImportMapper importMapper = new ImportMapper();
		List<ImportModel> importModels = tree.getImports().stream()
				.map(i -> importMapper.map(table, i))
				.toList();

		// Class declarations
		ClassMapper classMapper = new ClassMapper(packageModel, importModels);
		List<ClassModel> classModels = tree.getTypeDecls().stream()
				.filter(t -> t instanceof ClassTree)
				.map(t -> (ClassTree) t)
				.map(ct -> classMapper.map(table, ct))
				.toList();
		return new CompilationUnitModel(extractRange(table, tree), classModels);
	}
}
