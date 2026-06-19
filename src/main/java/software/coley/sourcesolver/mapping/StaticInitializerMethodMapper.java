package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BlockTree;
import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.model.MethodBodyModel;
import software.coley.sourcesolver.model.MethodModel;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.model.NameExpressionModel;
import software.coley.sourcesolver.model.TypeModel;
import software.coley.sourcesolver.util.Range;

import java.util.Collections;

public class StaticInitializerMethodMapper implements Mapper<MethodModel, BlockTree> {
	@Nonnull
	@Override
	public MethodModel map(@Nonnull MappingContext context, @Nonnull RangeExtractor extractor, @Nonnull BlockTree tree) {
		Range methodRange = extractor.get(tree); // Will contain the whole "static { ... }"
		Range keywordRange = new Range(methodRange.begin(), methodRange.begin() + 6);
		Range blockRange = new Range(context.getSource().indexOf('{', methodRange.begin()), methodRange.end());
		TypeModel.Primitive returnType = new TypeModel.Primitive(keywordRange, new NameExpressionModel(Range.UNKNOWN, "void"));
		ModifiersModel modifiers = new ModifiersModel(keywordRange, Collections.singleton("static"));
		MethodBodyModel body = context.map(MethodBodyMapper.class, tree);
		body = new MethodBodyModel(blockRange, body.getStatements()); // Shift the range forward to by rebuilding with 'blockRange'
		return new MethodModel(methodRange, "<clinit>", modifiers,
				Collections.emptyList(), returnType, Collections.emptyList(), null, Collections.emptyList(),
				Collections.emptyList(), body);
	}
}
