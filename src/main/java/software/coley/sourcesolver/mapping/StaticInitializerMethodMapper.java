package software.coley.sourcesolver.mapping;

import com.sun.source.tree.BlockTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.MethodModel;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.model.NameModel;
import software.coley.sourcesolver.model.TypeModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collections;

import static software.coley.sourcesolver.util.Range.extractRange;

public class StaticInitializerMethodMapper implements Mapper<MethodModel, BlockTree> {
	@Nonnull
	@Override
	public MethodModel map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nonnull BlockTree tree) {
		TypeModel.Primitive returnType = new TypeModel.Primitive(Range.UNKNOWN, new NameModel(Range.UNKNOWN, "void"));
		return new MethodModel(extractRange(table, tree), "<clinit>", ModifiersModel.EMPTY,
				Collections.emptyList(), returnType, Collections.emptyList(), null, Collections.emptyList(),
				Collections.emptyList(), context.map(MethodBodyMapper.class, tree));
	}
}
