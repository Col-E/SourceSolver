package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

public record BasicPrimitiveEntry(@Nonnull Kind kind, @Nonnull String descriptor) implements PrimitiveEntry {
	@Nonnull
	@Override
	public Kind getKind() {
		return kind;
	}

	@Nonnull
	@Override
	public String getDescriptor() {
		return descriptor;
	}
}
