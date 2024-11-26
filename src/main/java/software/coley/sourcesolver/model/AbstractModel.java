package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractModel implements Ranged {
	private final List<AbstractModel> children;
	private final Range range;

	protected AbstractModel(@Nonnull Range range) {
		this.range = range;
		this.children = Collections.emptyList();
	}

	protected AbstractModel(@Nonnull Range range, AbstractModel... children) {
		this.range = range;
		this.children = extractChildren(Arrays.stream(children));
	}

	protected AbstractModel(@Nonnull Range range, ChildSupplier... suppliers) {
		this.range = range;
		this.children = extractChildren(Arrays.stream(suppliers)
				.flatMap(supplier -> supplier.isSingle() ?
						Stream.of(supplier.getSingle()) : supplier.getMultiple().stream()));
	}

	@SuppressWarnings("unchecked")
	protected AbstractModel(@Nonnull Range range, @Nonnull Collection<? extends AbstractModel> children) {
		this.range = range;
		this.children = extractChildren((Stream<AbstractModel>) children.stream());
	}

	@Nonnull
	private static List<AbstractModel> extractChildren(@Nonnull Stream<AbstractModel> stream) {
		return stream.filter(c -> c != null && !c.range.isUnknown())
				.sorted(Comparator.comparing(AbstractModel::getRange))
				.toList();
	}

	@Nonnull
	public List<AbstractModel> getChildren() {
		return children;
	}

	@Nonnull
	@Override
	public Range getRange() {
		return range;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AbstractModel that = (AbstractModel) o;

		if (!range.equals(that.range)) return false;
		return !Objects.equals(children, that.children);
	}

	@Override
	public int hashCode() {
		int result = children != null ? children.hashCode() : 0;
		result = 31 * result + range.hashCode();
		return result;
	}
}
