package software.coley.sourcesolver.model;

import software.coley.sourcesolver.resolve.Resolver;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
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
		for (Model child : children)
			if (child instanceof AbstractModel abstractChild) abstractChild.parent = this;
	}

	@Nonnull
	private static List<Model> extractChildren(@Nonnull Stream<Model> stream) {
		return stream.filter(c -> c != null && !c.getRange().isUnknown())
				.sorted(Comparator.comparing(Model::getRange))
				.toList();
	}

	@Nonnull
	@Override
	public Resolution resolveAt(@Nonnull Resolver resolver, int position) {
		if (resolution == null)
			resolution = resolver.resolveAt(position, this);
		return resolution;
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
