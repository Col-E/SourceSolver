package software.coley.sourcesolver.mapping;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.TypeParameterModel;

import javax.annotation.Nonnull;
import javax.lang.model.element.Name;
import java.util.List;

import static software.coley.sourcesolver.util.Range.extractRange;

public class TypeParameterMapper {
	@Nonnull
	public TypeParameterModel map(@Nonnull EndPosTable table, @Nonnull TypeParameterTree tree) {
		List<? extends AnnotationTree> annotations = tree.getAnnotations();
		List<? extends Tree> bounds = tree.getBounds();
		Name name = tree.getName();
		return new TypeParameterModel(extractRange(table, tree));
	}
}
