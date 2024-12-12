package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.*;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.lang.model.element.Name;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class ClassMapper {
	private final PackageModel packageModel;
	private final List<ImportModel> importModels;

	public ClassMapper(@Nonnull PackageModel packageModel,
	                   @Nonnull List<ImportModel> importModels) {
		this.packageModel = packageModel;
		this.importModels = importModels;
	}

	@Nonnull
	public ClassModel map(@Nonnull EndPosTable table, @Nonnull ClassTree tree) {
		ModifiersMapper.ParsePair modifiersPair = new ModifiersMapper().map(table, tree.getModifiers());
		List<AnnotationUseModel> annotationModels = modifiersPair.annotationModels() == null ? Collections.emptyList() : modifiersPair.annotationModels();
		ModifiersModel modifiersModel = modifiersPair.isEmpty() ? ModifiersModel.EMPTY : modifiersPair.modifiers();

		Name className = tree.getSimpleName();

		List<? extends TypeParameterTree> typeParameters = tree.getTypeParameters();
		List<TypeParameterModel> typeParameterModels = typeParameters == null ?
				Collections.emptyList() :
				typeParameters.stream().map(t -> new TypeParameterMapper().map(table, t)).toList();

		Tree extendsClause = tree.getExtendsClause();
		NameModel extendsModel = extendsClause == null ? new NameModel(Range.UNKNOWN, "Object") : new NameMapper().map(table, extendsClause);

		List<? extends Tree> implementsClauses = tree.getImplementsClause();
		ImplementsModel implementsModel = implementsClauses.isEmpty() ?
				ImplementsModel.EMPTY :
				new ImplementsModel(extractRange(table, implementsClauses), implementsClauses.stream()
						.map(e -> new NameMapper().map(table, e))
						.toList());

		List<? extends Tree> permitsClause = tree.getPermitsClause();
		PermitsModel permitsModel = permitsClause.isEmpty() ?
				PermitsModel.EMPTY :
				new PermitsModel(extractRange(table, permitsClause), permitsClause.stream()
						.map(e -> new NameMapper().map(table, e))
						.toList());

		List<VariableModel> fieldModels = new ArrayList<>();
		List<MethodModel> methodModels = new ArrayList<>();
		List<ClassModel> innerClassModels = new ArrayList<>();
		VariableMapper variableMapper = new VariableMapper();
		MethodMapper methodMapper = new MethodMapper();
		for (Tree memberTree : tree.getMembers()) {
			if (memberTree instanceof MethodTree methodTree) {
				MethodModel methodModel = methodMapper.map(table, methodTree);
				methodModels.add(methodModel);
			} else if (memberTree instanceof VariableTree variableTree) {
				VariableModel fieldModel = variableMapper.map(table, variableTree);
				fieldModels.add(fieldModel);
			} else if (memberTree instanceof ClassTree innerClassTree) {
				ClassModel innerClassModel = map(table, innerClassTree);
				innerClassModels.add(innerClassModel);
			} else if (memberTree instanceof BlockTree staticInitializerTree) {
				methodModels.add(methodMapper.mapStaticInitializer(table, staticInitializerTree));
			}
		}

		return new ClassModel(extractRange(table, tree), packageModel, importModels, annotationModels, modifiersModel,
				className.toString(), typeParameterModels, extendsModel, implementsModel, permitsModel, fieldModels, methodModels, innerClassModels);
	}
}
