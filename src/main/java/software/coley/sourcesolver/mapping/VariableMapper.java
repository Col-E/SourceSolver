package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.model.TypeModel;
import software.coley.sourcesolver.model.VariableModel;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class VariableMapper implements Mapper<VariableModel, VariableTree> {
	@Nonnull
	@Override
	@SuppressWarnings("DataFlowIssue")
	public VariableModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull VariableTree tree) {
		ModifiersMapper.ModifiersParsePair modifiersPair = context.map(ModifiersMapper.class, tree.getModifiers());
		List<AnnotationExpressionModel> annotationModels = modifiersPair.getAnnotations() == null ? Collections.emptyList() : modifiersPair.getAnnotations();
		ModifiersModel modifiers = modifiersPair.isEmpty() ? ModifiersModel.EMPTY : modifiersPair.getModifiers();

		TypeModel typeModel = context.mapOr(TypeMapper.class, tree.getType(), TypeModel::newVar);
		String name = tree.getName().toString();

		ExpressionTree initializer = tree.getInitializer();
		Model valueModel = initializer == null ? null : context.map(ExpressionMapper.class, initializer);

		return new VariableModel(extractRange(table, tree), annotationModels, modifiers, typeModel, name, valueModel);
	}
}
