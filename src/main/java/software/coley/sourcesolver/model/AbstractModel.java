package software.coley.sourcesolver.model;

import software.coley.sourcesolver.resolve.Resolver;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractModel implements Ranged {
	private final List<AbstractModel> children;
	private final Range range;
	private AbstractModel parent;
	private Resolution resolution;

	protected AbstractModel(@Nonnull Range range) {
		this.range = range;
		this.children = Collections.emptyList();
	}

	protected AbstractModel(@Nonnull Range range, AbstractModel... children) {
		this.range = range;
		this.children = extractChildren(Arrays.stream(children));
		for (AbstractModel child : children)
			child.parent = this;
	}

	protected AbstractModel(@Nonnull Range range, ChildSupplier... suppliers) {
		this.range = range;
		this.children = extractChildren(Arrays.stream(suppliers)
				.flatMap(supplier -> supplier.isSingle() ?
						Stream.of(supplier.getSingle()) : supplier.getMultiple().stream()));
		for (AbstractModel child : children)
			child.parent = this;
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
	public Resolution resolve(@Nonnull Resolver resolver) {
		int index = range.begin();
		if (index < 0 && parent != null)
			index = parent.range.begin();
		return resolveAt(resolver, index);
	}

	@Nonnull
	public Resolution resolveAt(@Nonnull Resolver resolver, int index) {
		if (resolution == null)
			resolution = resolver.resolveAt(index, this);
		return resolution;
	}

	@Nullable
	public AbstractModel getChildAtPosition(int position) {
		for (AbstractModel child : children)
			if (child.range.isWithin(position))
				return child;
		return null;
	}

	@Nonnull
	public String getSource(@Nonnull CompilationUnitModel unit) {
		String src = unit.getInputSource();
		int begin = Math.max(0, range.begin());
		int end = Math.min(src.length(), range.end());
		return src.substring(begin, end);
	}

	@Nonnull
	public List<AbstractModel> getChildren() {
		return children;
	}

	@Nullable
	public AbstractModel getParent() {
		return parent;
	}

	@Nonnull
	@Override
	public Range getRange() {
		return range;
	}

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();
}
