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

public abstract class AbstractModel implements Model {
	private final List<Model> children;
	private final Range range;
	private Model parent;
	private Resolution resolution;

	protected AbstractModel(@Nonnull Range range) {
		this.range = range;
		this.children = Collections.emptyList();
	}

	protected AbstractModel(@Nonnull Range range, Model... children) {
		this.range = range;
		this.children = extractChildren(Arrays.stream(children));
		for (Model child : children)
			if (child instanceof AbstractModel abstractChild) abstractChild.parent = this;
	}

	protected AbstractModel(@Nonnull Range range, ChildSupplier... suppliers) {
		this.range = range;
		this.children = extractChildren(Arrays.stream(suppliers)
				.flatMap(supplier -> supplier.isSingle() ?
						Stream.of(supplier.getSingle()) : supplier.getMultiple().stream()));
		for (Model child : children)
			if (child instanceof AbstractModel abstractChild) abstractChild.parent = this;
	}

	@SuppressWarnings("unchecked")
	protected AbstractModel(@Nonnull Range range, @Nonnull Collection<? extends Model> children) {
		this.range = range;
		this.children = extractChildren((Stream<Model>) children.stream());
	}

	@Nonnull
	private static List<Model> extractChildren(@Nonnull Stream<Model> stream) {
		return stream.filter(c -> c != null && !c.getRange().isUnknown())
				.sorted(Comparator.comparing(Model::getRange))
				.toList();
	}

	@Nonnull
	@Override
	public Resolution resolve(@Nonnull Resolver resolver) {
		int index = range.begin();
		if (index < 0 && parent != null)
			index = parent.getRange().begin();
		return resolveAt(resolver, index);
	}

	@Nonnull
	@Override
	public Resolution resolveAt(@Nonnull Resolver resolver, int index) {
		if (resolution == null)
			resolution = resolver.resolveAt(index, this);
		return resolution;
	}

	@Nullable
	@Override
	public Model getChildAtPosition(int position) {
		for (Model child : children)
			if (child.getRange().isWithin(position))
				return child;
		return null;
	}

	@Nonnull
	@Override
	public String getSource(@Nonnull CompilationUnitModel unit) {
		String src = unit.getInputSource();
		int begin = Math.max(0, range.begin());
		int end = Math.min(src.length(), range.end());
		return src.substring(begin, end);
	}

	@Nonnull
	@Override
	public List<Model> getChildren() {
		return children;
	}

	@Nullable
	@Override
	public Model getParent() {
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
