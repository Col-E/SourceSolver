package software.coley.sourcesolver.mapping;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.*;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MethodMapper {
	@Nonnull
	public MethodModel map(@Nonnull EndPosTable table, @Nonnull MethodTree tree) {
		// Modifiers
		ModifiersMapper.ParsePair modifiersPair = new ModifiersMapper().map(table, tree.getModifiers());
		List<AnnotationUseModel> annotationModels = modifiersPair.annotationModels() == null ? Collections.emptyList() : modifiersPair.annotationModels();
		ModifiersModel modifiers = modifiersPair.isEmpty() ? ModifiersModel.EMPTY : modifiersPair.modifiers();

		// Type param
		TypeParameterMapper typeParameterMapper = new TypeParameterMapper();
		List<TypeParameterModel> typeParameters = tree.getTypeParameters().stream().map(t -> typeParameterMapper.map(table, t)).toList();

		// Return type + parameters
		TypeModel returnType = new TypeMapper().map(table, tree.getReturnType());
		VariableMapper variableMapper = new VariableMapper();
		List<VariableModel> parameters = tree.getParameters().stream().map(p -> variableMapper.map(table, p)).toList();

		// throws X,Y,Z
		ExpressionMapper expressionMapper = new ExpressionMapper();
		List<AbstractModel> thrownTypes = tree.getThrows().stream().map(t -> expressionMapper.map(table, t)).toList();

		// default value for annotation methods
		AbstractModel defaultValue;
		Tree defaultValueTree = tree.getDefaultValue();
		if (defaultValueTree instanceof LiteralTree literalValue) {
			// primitives + strings
			defaultValue = new LiteralMapper().map(table, literalValue);
		} else if (defaultValueTree instanceof MemberSelectTree enumValue) {
			// enums
			defaultValue = new MemberSelectMapper().map(table, enumValue);
		} else if (defaultValueTree instanceof AnnotationTree annotationValue) {
			// inner annotations
			defaultValue = new AnnotationUseMapper().map(table, annotationValue);
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
		MethodBodyModel methodBody = mapMethodBody(table, tree.getBody());

		return new MethodModel(extractRange(table, tree), name, modifiers, typeParameters, returnType,
				parameters, defaultValue, thrownTypes, annotationModels, methodBody);
	}

	@Nonnull
	public MethodModel mapStaticInitializer(@Nonnull EndPosTable table, @Nonnull BlockTree staticInitializerTree) {
		TypeModel.Primitive returnType = new TypeModel.Primitive(Range.UNKNOWN, new NameModel(Range.UNKNOWN, "void"));
		return new MethodModel(extractRange(table, staticInitializerTree), "<clinit>", ModifiersModel.EMPTY,
				Collections.emptyList(), returnType, Collections.emptyList(), null, Collections.emptyList(),
				Collections.emptyList(), mapMethodBody(table, staticInitializerTree));
	}

	@Nullable
	public MethodBodyModel mapMethodBody(@Nonnull EndPosTable table, @Nullable BlockTree blockTree) {
		if (blockTree == null) return null;
		StatementMapper statementMapper = new StatementMapper();
		List<AbstractStatementModel> list = blockTree.getStatements().stream().map(s -> statementMapper.map(table, s)).toList();
		return new MethodBodyModel(extractRange(table, blockTree), list);
	}
}
