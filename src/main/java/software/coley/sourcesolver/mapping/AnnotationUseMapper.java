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

public class AnnotationUseMapper implements Mapper<AnnotationUseModel, AnnotationTree> {
	@Nonnull
	@Override
	public AnnotationUseModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull AnnotationTree tree) {
		Tree annotationType = tree.getAnnotationType();

		List<? extends ExpressionTree> arguments = tree.getArguments();
		List<AnnotationArgumentModel> argumentModels = new ArrayList<>(arguments.size());
		for (ExpressionTree argument : arguments) {
			if (argument instanceof AssignmentTree argumentAssign) {
				// Map value model based on "arg=value"
				NameModel nameModel = context.map(NameMapper.class, argumentAssign.getVariable());
				AbstractModel valueModel = context.map(ExpressionMapper.class, argumentAssign.getExpression());
				argumentModels.add(new AnnotationArgumentModel(extractRange(table, argument), nameModel, valueModel));
			} else {
				// Variable name is implied to be "value"
				AbstractModel valueModel = context.map(ExpressionMapper.class, argument);
				argumentModels.add(new AnnotationArgumentModel(extractRange(table, argument), null, valueModel));
			}
		}

		NameModel name = context.map(NameMapper.class, annotationType);
		return new AnnotationUseModel(extractRange(table, tree), name, argumentModels);
	}
}
