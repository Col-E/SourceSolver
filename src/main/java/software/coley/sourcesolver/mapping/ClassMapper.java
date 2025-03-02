package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.EndPosTable;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.ClassModel;
import software.coley.sourcesolver.model.ImplementsModel;
import software.coley.sourcesolver.model.MethodModel;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.model.NamedModel;
import software.coley.sourcesolver.model.PermitsModel;
import software.coley.sourcesolver.model.TypeParameterModel;
import software.coley.sourcesolver.model.VariableModel;
import software.coley.sourcesolver.util.Range;

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

		// Get the class name. In some edge cases (decompiled code) we may need to "correct" javac's interpretation.
		// For instance, decompiling an inner class in isolation can yield 'Outer.Inner' and the simple name will
		// only include "Outer". We need to catch those and fix the name here so resolving later on knows we're actually
		// in an inner class.
		String className = tree.getSimpleName().toString();
		nameFix:
		{
			// Get the rough range of where this class name is defined.
			Range classRange = extractRange(table, tree);
			int classStart = classRange.begin();
			if (classStart < 0)
				break nameFix;
			String source = context.getSource();
			int endIndex = source.indexOf('\n', classStart);
			while (endIndex > 0 && !source.substring(classStart, endIndex).contains(className)) {
				// Move forward line by line until the range has the name defined.
				// We do this because the 'start' may otherwise be defined by annotations applied to the
				// class, and not the part we want (the "class <name> {" section)
				classStart = endIndex + 1;
				endIndex = source.indexOf('\n', classStart);
			}
			if (endIndex < 0 || classStart < 0)
				break nameFix; // Skip if range invalid.


			// Skip if the definition based on the given range doesn't include the current assumed class name.
			String definitionLine = source.substring(classStart, endIndex);
			if (!definitionLine.contains(className))
				break nameFix;

			// See if there are any ".name" patterns after the current name.
			// If so, append those patterns.
			int nameStart = definitionLine.indexOf(className);
			StringBuilder classNameBuilder = new StringBuilder(className);
			for (int i = nameStart + classNameBuilder.length(); i < endIndex; i++) {
				char c = definitionLine.charAt(i);
				if (c == '.' || c == '$') {
					classNameBuilder.append('$');
				} else if (Character.isJavaIdentifierPart(c)) {
					classNameBuilder.append(c);
				} else {
					break;
				}
			}
			className = classNameBuilder.toString();
		}
		context.setClassName(className);

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
				className, typeParameterModels, extendsModel, implementsModel, permitsModel, fieldModels, methodModels, innerClassModels);
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
