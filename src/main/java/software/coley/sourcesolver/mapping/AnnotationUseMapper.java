package software.coley.sourcesolver.mapping;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.AbstractExpressionModel;
import software.coley.sourcesolver.model.AnnotationArgumentModel;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.NameExpressionModel;

import java.util.ArrayList;
import java.util.List;

public class AnnotationUseMapper implements Mapper<AnnotationExpressionModel, AnnotationTree> {
	@Nonnull
	@Override
	public AnnotationExpressionModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull AnnotationTree tree) {
		Tree annotationType = tree.getAnnotationType();

		List<? extends ExpressionTree> arguments = tree.getArguments();
		List<AnnotationArgumentModel> argumentModels = new ArrayList<>(arguments.size());
		for (ExpressionTree argument : arguments) {
			if (argument instanceof AssignmentTree argumentAssign) {
				// Map value model based on "arg=value"
				NameExpressionModel nameModel = context.map(NameMapper.class, argumentAssign.getVariable());
				AbstractExpressionModel valueModel = context.map(ExpressionMapper.class, argumentAssign.getExpression());
				argumentModels.add(new AnnotationArgumentModel(extractor.get(argument), nameModel, valueModel));
			} else {
				// Variable name is implied to be "value"
				AbstractExpressionModel valueModel = context.map(ExpressionMapper.class, argument);
				argumentModels.add(new AnnotationArgumentModel(extractor.get(argument), null, valueModel));
			}
		}

		NameExpressionModel name = context.map(NameMapper.class, annotationType);
		return new AnnotationExpressionModel(extractor.get(tree), name, argumentModels);
	}
}
