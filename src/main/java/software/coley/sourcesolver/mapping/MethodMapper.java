package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AnnotationUseModel;
import software.coley.sourcesolver.model.MethodModel;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.model.VariableModel;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class MethodMapper {
	@Nonnull
	public MethodModel map(@Nonnull EndPosTable table, @Nonnull MethodTree tree) {
		ModifiersMapper.ParsePair modifiersPair = new ModifiersMapper().map(table, tree.getModifiers());
		List<AnnotationUseModel> annotationModels = modifiersPair.annotationModels() == null ? Collections.emptyList() : modifiersPair.annotationModels();
		ModifiersModel modifiers = modifiersPair.isEmpty() ? ModifiersModel.EMPTY : modifiersPair.modifiers();

		List<? extends TypeParameterTree> typeParameters = tree.getTypeParameters();

		// TODO: We need a AbstractTypeModel or interface Typed with impls
		//  - Primitive
		//  - Object
		//  - ParameterizedObject
		//  - Array
		Tree type = tree.getReturnType();
		if (type instanceof PrimitiveTypeTree primitive) {

		} else if (type instanceof IdentifierTree identifier){

		} else if (type instanceof ParameterizedTypeTree parameterizedType) {

		} else if (type instanceof ArrayTypeTree arrayType) {

		}

		VariableTree receiverParameter = tree.getReceiverParameter();
		List<? extends VariableTree> parameters = tree.getParameters();
		List<? extends ExpressionTree> aThrows = tree.getThrows();
		Tree defaultValue = tree.getDefaultValue();

		// TODO: If the type is malformed, then the name is going to be <error>
		//  example: void[] array;
		//   --> The type becomes "array" too which is wrong
		//       Not much we can really do tbh
		String name = tree.getName().toString();

		// annotationModels, modifiers, typeModel, name, valueModel
		return new MethodModel(extractRange(table, tree));
	}
}
