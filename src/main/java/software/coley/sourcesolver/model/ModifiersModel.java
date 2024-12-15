package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

public class ModifiersModel extends AbstractModel {
	public static final ModifiersModel EMPTY = new ModifiersModel(Range.UNKNOWN, Collections.emptySet());
	private static final List<String> ORDER = List.of(
			"public",
			"protected",
			"private",
			"abstract",
			"static",
			"final",
			"open",
			"mandated",
			"synthetic",
			"synchronized",
			"native",
			"transient",
			"volatile",
			"strictfp",
			"interface",
			"annotation-interface",
			"enum",
			"module"
	);
	private final NavigableSet<String> modifiers;

	public ModifiersModel(@Nonnull Range range, @Nonnull Collection<String> modifiers) {
		super(range);
		this.modifiers = sortModifiers(modifiers);
	}

	@Nonnull
	private static NavigableSet<String> sortModifiers(@Nonnull Collection<String> modifiers) {
		TreeSet<String> set = new TreeSet<>((o1, o2) -> {
			int i1 = ORDER.indexOf(o1);
			int i2 = ORDER.indexOf(o2);
			int cmp = Integer.compare(i1, i2);
			if (cmp == 0)
				cmp = o1.compareToIgnoreCase(o2);
			return cmp;
		});
		set.addAll(modifiers);
		return set;
	}

	@Nonnull
	public NavigableSet<String> getModifiers() {
		return modifiers;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ModifiersModel that = (ModifiersModel) o;

		return Objects.equals(modifiers, that.modifiers);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + (modifiers != null ? modifiers.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return String.join(" ", modifiers);
	}
}
