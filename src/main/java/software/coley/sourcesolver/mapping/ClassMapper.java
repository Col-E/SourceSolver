package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.*;
import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import javax.lang.model.element.Name;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class ClassMapper implements Mapper<ClassModel, ClassTree> {
	@Nonnull
	@Override
	public ClassModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull ClassTree tree) {
		ModifiersMapper.ModifiersParsePair modifiersPair = context.map(ModifiersMapper.class, tree.getModifiers());
		List<AnnotationExpressionModel> annotationModels = modifiersPair.getAnnotations() == null ? Collections.emptyList() : modifiersPair.getAnnotations();
		ModifiersModel modifiersModel = modifiersPair.isEmpty() ? ModifiersModel.EMPTY : modifiersPair.getModifiers();

		Name className = tree.getSimpleName();

		List<? extends TypeParameterTree> typeParameters = tree.getTypeParameters();
		List<TypeParameterModel> typeParameterModels = typeParameters == null ?
				Collections.emptyList() :
				typeParameters.stream().map(t -> context.map(TypeParameterMapper.class, t)).toList();

		Tree extendsClause = tree.getExtendsClause();
		NamedModel extendsModel = extendsClause == null ? new NameExpressionModel(Range.UNKNOWN, "Object") : mapMaybeGeneric(context, extendsClause);

		List<? extends Tree> implementsClauses = tree.getImplementsClause();
		ImplementsModel implementsModel = implementsClauses.isEmpty() ?
				ImplementsModel.EMPTY :
				new ImplementsModel(extractRange(table, implementsClauses), implementsClauses.stream()
						.map(e -> mapMaybeGeneric(context, e))
						.toList());

		List<? extends Tree> permitsClause = tree.getPermitsClause();
		PermitsModel permitsModel = permitsClause.isEmpty() ?
				PermitsModel.EMPTY :
				new PermitsModel(extractRange(table, permitsClause), permitsClause.stream()
						.map(e -> mapMaybeGeneric(context, e))
						.toList());

		List<VariableModel> fieldModels = new ArrayList<>();
		List<MethodModel> methodModels = new ArrayList<>();
		List<ClassModel> innerClassModels = new ArrayList<>();
		for (Tree memberTree : tree.getMembers()) {
			if (memberTree instanceof MethodTree methodTree) {
				MethodModel methodModel = context.map(MethodMapper.class, methodTree);
				methodModels.add(methodModel);
			} else if (memberTree instanceof VariableTree variableTree) {
				VariableModel fieldModel = context.map(VariableMapper.class, variableTree);
				fieldModels.add(fieldModel);
			} else if (memberTree instanceof ClassTree innerClassTree) {
				ClassModel innerClassModel = map(context, table, innerClassTree);
				innerClassModels.add(innerClassModel);
			} else if (memberTree instanceof BlockTree staticInitializerTree) {
				methodModels.add(context.map(StaticInitializerMethodMapper.class, staticInitializerTree));
			}
		}

		return new ClassModel(extractRange(table, tree), annotationModels, modifiersModel,
				className.toString(), typeParameterModels, extendsModel, implementsModel, permitsModel, fieldModels, methodModels, innerClassModels);
	}

	@Nonnull
	private NamedModel mapMaybeGeneric(@Nonnull MappingContext context, @Nonnull Tree tree) {
		if (tree instanceof ParameterizedTypeTree parameterizedType)
			// Used for extending/implementing types with type arguments, such as:
			//  extends AbstractList<E>
			//  implements List<E>
			// We cast to named because the type impls that can be extended/implemented are always named.
			return (NamedModel) context.map(TypeMapper.class, parameterizedType);
		return context.map(NameMapper.class, tree);
	}
}
