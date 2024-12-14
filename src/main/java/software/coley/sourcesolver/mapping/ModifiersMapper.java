package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ModifiersTree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.util.Range.extractRange;

public class ModifiersMapper implements Mapper<ModifiersMapper.ModifiersParsePair, ModifiersTree> {
	@Nonnull
	@Override
	public ModifiersParsePair map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nullable ModifiersTree tree) {
		if (tree == null)
			return new ModifiersParsePair(Collections.emptyList(), null);

		List<AnnotationExpressionModel> annotationModels = tree.getAnnotations().stream()
				.map(anno -> context.map(AnnotationUseMapper.class, anno))
				.toList();
		Set<String> modifierNames = tree.getFlags().stream()
				.map(m -> m.name().toLowerCase().replace('_', '-'))
				.collect(Collectors.toUnmodifiableSet());
		return new ModifiersParsePair(annotationModels, new ModifiersModel(extractRange(table, tree), modifierNames));
	}

	/**
	 * Intermediate holder for the annotations preceding the modifiers and the actual modifiers.
	 * The language specification has annotations go here as it is a common element preceding classes,
	 * fields, and methods.
	 */
	public static class ModifiersParsePair extends AbstractModel {
		private final List<AnnotationExpressionModel> annotationModels;
		private final ModifiersModel modifiers;

		public ModifiersParsePair(@Nullable List<AnnotationExpressionModel> annotationModels,
		                          @Nullable ModifiersModel modifiers) {
			super(Range.UNKNOWN);
			this.annotationModels = annotationModels;
			this.modifiers = modifiers;
		}

		@Nullable
		public List<AnnotationExpressionModel> getAnnotationModels() {
			return annotationModels;
		}

		@Nullable
		public ModifiersModel getModifiers() {
			return modifiers;
		}

		public boolean isEmpty() {
			return modifiers == null || modifiers.getModifiers().isEmpty();
		}
	}
}
