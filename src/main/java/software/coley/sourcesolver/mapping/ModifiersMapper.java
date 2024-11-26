package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ModifiersTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AnnotationUseModel;
import software.coley.sourcesolver.model.ModifiersModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.util.Range.extractRange;

public class ModifiersMapper {
	@Nonnull
	public ParsePair map(@Nonnull EndPosTable table, @Nullable ModifiersTree tree) {
		if (tree == null)
			return new ParsePair(Collections.emptyList(), null);

		AnnotationUseMapper annotationMapper = new AnnotationUseMapper();
		List<AnnotationUseModel> annotationModels = tree.getAnnotations().stream()
				.map(anno -> annotationMapper.map(table, anno))
				.toList();
		Set<String> modifierNames = tree.getFlags().stream()
				.map(m -> m.name().toLowerCase().replace('_', '-'))
				.collect(Collectors.toUnmodifiableSet());
		return new ParsePair(annotationModels, new ModifiersModel(extractRange(table, tree), modifierNames));
	}

	public record ParsePair(@Nullable List<AnnotationUseModel> annotationModels,
	                        @Nullable ModifiersModel modifiers) {
		public boolean isEmpty() {
			return modifiers == null || modifiers.getModifiers().isEmpty();
		}
	}
}
