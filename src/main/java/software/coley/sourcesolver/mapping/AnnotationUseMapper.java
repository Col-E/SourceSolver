package software.coley.sourcesolver.mapping;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AnnotationArgumentModel;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.NameExpressionModel;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class AnnotationUseMapper implements Mapper<AnnotationExpressionModel, AnnotationTree> {
	@Nonnull
	@Override
	public AnnotationExpressionModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull AnnotationTree tree) {
		Tree annotationType = tree.getAnnotationType();

		List<? extends ExpressionTree> arguments = tree.getArguments();
		List<AnnotationArgumentModel> argumentModels = new ArrayList<>(arguments.size());
		for (ExpressionTree argument : arguments) {
			if (argument instanceof AssignmentTree argumentAssign) {
				// Map value model based on "arg=value"
				NameExpressionModel nameModel = context.map(NameMapper.class, argumentAssign.getVariable());
				AbstractExpressionModel valueModel = context.map(ExpressionMapper.class, argumentAssign.getExpression());
				argumentModels.add(new AnnotationArgumentModel(extractRange(table, argument), nameModel, valueModel));
			} else {
				// Variable name is implied to be "value"
				AbstractExpressionModel valueModel = context.map(ExpressionMapper.class, argument);
				argumentModels.add(new AnnotationArgumentModel(extractRange(table, argument), null, valueModel));
			}
		}

		NameExpressionModel name = context.map(NameMapper.class, annotationType);
		return new AnnotationExpressionModel(extractRange(table, tree), name, argumentModels);
	}
}
