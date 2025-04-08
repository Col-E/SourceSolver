package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ModifiersTree;
import com.sun.tools.javac.tree.EndPosTable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.model.AnnotationExpressionModel;
import software.coley.sourcesolver.model.ModifiersModel;
import software.coley.sourcesolver.util.Range;

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

		// Annotations being bundled with the modifiers means we cannot trust the existing range of this tree element.
		// We need to manipulate it a bit if annotations are present.
		Range range = extractRange(table, tree).shrink(annotationModels);

		return new ModifiersParsePair(annotationModels, new ModifiersModel(range, modifierNames));
	}

	/**
	 * Intermediate holder for the annotations preceding the modifiers and the actual modifiers.
	 * The language specification has annotations go here as it is a common element preceding classes,
	 * fields, and methods.
	 */
	public static class ModifiersParsePair extends AbstractModel {
		private final List<AnnotationExpressionModel> annotations;
		private final ModifiersModel modifiers;

		public ModifiersParsePair(@Nullable List<AnnotationExpressionModel> annotations,
		                          @Nullable ModifiersModel modifiers) {
			super(Range.UNKNOWN);
			this.annotations = annotations;
			this.modifiers = modifiers;
		}

		@Nullable
		public List<AnnotationExpressionModel> getAnnotations() {
			return annotations;
		}

		@Nullable
		public ModifiersModel getModifiers() {
			return modifiers;
		}

		public boolean isEmpty() {
			return modifiers == null || modifiers.getModifiers().isEmpty();
		}

		@Override
		public boolean equals(Object o) {
			return o == this;
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}
}
