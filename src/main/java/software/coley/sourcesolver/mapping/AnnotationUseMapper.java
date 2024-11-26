package software.coley.sourcesolver.mapping;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.AnnotationArgumentModel;
import software.coley.sourcesolver.model.AnnotationUseModel;
import software.coley.sourcesolver.model.NameModel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class AnnotationUseMapper {
	@Nonnull
	public AnnotationUseModel map(@Nonnull EndPosTable table, AnnotationTree tree) {
		Tree annotationType = tree.getAnnotationType();

		List<? extends ExpressionTree> arguments = tree.getArguments();
		List<AnnotationArgumentModel> argumentModels = new ArrayList<>(arguments.size());
		ExpressionMapper expressionMapper = new ExpressionMapper();
		for (ExpressionTree argument : arguments) {
			if (argument instanceof AssignmentTree argumentAssign) {
				// Map value model based on "arg=value"
				NameModel nameModel = new NameMapper().map(table, argumentAssign.getVariable());
				AbstractModel valueModel = expressionMapper.map(table, argumentAssign.getExpression());
				argumentModels.add(new AnnotationArgumentModel(extractRange(table, argument), nameModel, valueModel));
			} else {
				// Variable name is implied to be "value"
				AbstractModel valueModel = expressionMapper.map(table, argument);
				argumentModels.add(new AnnotationArgumentModel(extractRange(table, argument), null, valueModel));
			}
		}

		NameModel name = new NameMapper().map(table, annotationType);
		return new AnnotationUseModel(extractRange(table, tree), name, argumentModels);
	}
}
