package software.coley.sourcesolver.mapping;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.LiteralExpressionModel;
import software.coley.sourcesolver.model.MethodBodyModel;
import software.coley.sourcesolver.model.MethodModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.model.TypeModel;
import software.coley.sourcesolver.model.TypeParameterModel;
import software.coley.sourcesolver.model.VariableModel;
import software.coley.sourcesolver.util.Range;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MethodMapper implements Mapper<MethodModel, MethodTree> {
	@Nonnull
	@Override
	public MethodModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull MethodTree tree) {
		// Modifiers
		ModifiersMapper.ModifiersParsePair modifiersPair = context.map(ModifiersMapper.class, tree.getModifiers());
		List<AnnotationExpressionModel> annotationModels = modifiersPair.getAnnotations() == null ? Collections.emptyList() : modifiersPair.getAnnotations();
		ModifiersModel modifiers = modifiersPair.isEmpty() ? ModifiersModel.EMPTY : modifiersPair.getModifiers();

		// Type param
		List<TypeParameterModel> typeParameters = tree.getTypeParameters().stream().map(t -> context.map(TypeParameterMapper.class, t)).toList();

		// Return type + parameters
		//  - Constructors are a special case, so we manually make a 'void' return for those
		TypeModel returnType = tree.getReturnType() == null ?
				new TypeModel.Primitive(Range.UNKNOWN, new LiteralExpressionModel(Range.UNKNOWN, LiteralExpressionModel.Kind.VOID, "void")) :
				context.map(TypeMapper.class, tree.getReturnType());
		List<VariableModel> parameters = tree.getParameters().stream().map(p -> context.map(VariableMapper.class, p)).toList();

		// throws X,Y,Z
		List<AbstractExpressionModel> thrownTypes = tree.getThrows().stream().map(t -> context.map(ExpressionMapper.class, t)).toList();

		// default value for annotation methods
		Model defaultValue;
		Tree defaultValueTree = tree.getDefaultValue();
		defaultValue = switch (defaultValueTree) {
			case LiteralTree literalValue -> context.map(LiteralMapper.class, literalValue);
			case MemberSelectTree enumValue -> context.map(MemberSelectMapper.class, enumValue);
			case AnnotationTree annotationValue -> context.map(AnnotationUseMapper.class, annotationValue);
			case null, default -> null;
		};

		// Note: If the type is malformed, then the name is going to be <error>
		//  example: void[] array;
		//   --> The type becomes "array" too which is wrong
		//       Not much we can really do tbh
		//
		// Note: No tree for the name, so just use a string
		//
		// Note: Just like the problem mentioned with names in 'ClassMapper'
		//  isolated inner classes can be misinterpreted, and we need to fix those manually.
		//  However, in this case the indicator for this happening is different.
		String name = tree.getName().toString();
		if ("<error>".equals(name)) {
			// If we see that the return type is the "constructor name" in reality then we should reasonably
			// assume this has been misinterpreted as a normal (but busted) method instead of as a constructor.
			String returnTypeString = returnType.getIdentifier().toString().replace('.', '$');
			if (returnTypeString.startsWith(context.getClassName())) {
				name = "<init>";
				returnType = new TypeModel.Primitive(Range.UNKNOWN,
						new LiteralExpressionModel(Range.UNKNOWN, LiteralExpressionModel.Kind.VOID, "void"));
			}
		}

		// Method body { ... }
		BlockTree body = tree.getBody();
		MethodBodyModel methodBody = body == null ? null : context.map(MethodBodyMapper.class, body);

		return new MethodModel(extractRange(table, tree), name, modifiers, typeParameters, returnType,
				parameters, defaultValue, thrownTypes, annotationModels, methodBody);
	}
}
