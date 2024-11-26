package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.AnnotationUseModel;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.model.TypeModel;
import software.coley.sourcesolver.model.VariableModel;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class VariableMapper {
	@Nonnull
	@SuppressWarnings("DataFlowIssue")
	public VariableModel map(@Nonnull EndPosTable table, @Nonnull VariableTree tree) {
		ModifiersMapper.ParsePair modifiersPair = new ModifiersMapper().map(table, tree.getModifiers());
		List<AnnotationUseModel> annotationModels = modifiersPair.annotationModels() == null ? Collections.emptyList() : modifiersPair.annotationModels();
		ModifiersModel modifiers = modifiersPair.isEmpty() ? ModifiersModel.EMPTY : modifiersPair.modifiers();

		TypeModel typeModel = new TypeMapper().map(table, tree.getType());
		String name = tree.getName().toString();

		ExpressionTree initializer = tree.getInitializer();
		AbstractModel valueModel = initializer == null ? null : new ExpressionMapper().map(table, initializer);

		return new VariableModel(extractRange(table, tree), annotationModels, modifiers, typeModel, name, valueModel);
	}
}
