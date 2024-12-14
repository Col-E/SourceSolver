package software.coley.sourcesolver.mapping;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.MethodBodyModel;
import software.coley.sourcesolver.model.MethodModel;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.model.TypeModel;
import software.coley.sourcesolver.model.TypeParameterModel;
import software.coley.sourcesolver.model.VariableModel;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MethodMapper implements Mapper<MethodModel, MethodTree> {
	@Nonnull
	@Override
	public MethodModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull MethodTree tree) {
		// Modifiers
		ModifiersMapper.ModifiersParsePair modifiersPair = context.map(ModifiersMapper.class, tree.getModifiers());
		List<AnnotationExpressionModel> annotationModels = modifiersPair.getAnnotationModels() == null ? Collections.emptyList() : modifiersPair.getAnnotationModels();
		ModifiersModel modifiers = modifiersPair.isEmpty() ? ModifiersModel.EMPTY : modifiersPair.getModifiers();

		// Type param
		List<TypeParameterModel> typeParameters = tree.getTypeParameters().stream().map(t -> context.map(TypeParameterMapper.class, t)).toList();

		// Return type + parameters
		TypeModel returnType = context.map(TypeMapper.class, tree.getReturnType());
		List<VariableModel> parameters = tree.getParameters().stream().map(p -> context.map(VariableMapper.class, p)).toList();

		// throws X,Y,Z
		List<AbstractExpressionModel> thrownTypes = tree.getThrows().stream().map(t -> context.map(ExpressionMapper.class, t)).toList();

		// default value for annotation methods
		AbstractModel defaultValue;
		Tree defaultValueTree = tree.getDefaultValue();
		if (defaultValueTree instanceof LiteralTree literalValue) {
			// primitives + strings
			defaultValue = context.map(LiteralMapper.class, literalValue);
		} else if (defaultValueTree instanceof MemberSelectTree enumValue) {
			// enums
			defaultValue = context.map(MemberSelectMapper.class, enumValue);
		} else if (defaultValueTree instanceof AnnotationTree annotationValue) {
			// inner annotations
			defaultValue = context.map(AnnotationUseMapper.class, annotationValue);
		} else {
			defaultValue = null;
		}

		// Note: If the type is malformed, then the name is going to be <error>
		//  example: void[] array;
		//   --> The type becomes "array" too which is wrong
		//       Not much we can really do tbh
		//
		// Note: No tree for the name, so just use a string
		String name = tree.getName().toString();

		// Method body { ... }
		BlockTree body = tree.getBody();
		MethodBodyModel methodBody = body == null ? null : context.map(MethodBodyMapper.class, body);

		return new MethodModel(extractRange(table, tree), name, modifiers, typeParameters, returnType,
				parameters, defaultValue, thrownTypes, annotationModels, methodBody);
	}
}
