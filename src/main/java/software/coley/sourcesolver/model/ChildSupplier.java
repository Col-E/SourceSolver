package software.coley.sourcesolver.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public interface ChildSupplier {
	@Nonnull
	static ChildSupplier of(@Nullable Model model) {
		if (model == null) return new ChildSupplier() {
			@Override
			public boolean isSingle() {
				return false;
			}

			@Nonnull
			@Override
			public Model getSingle() {
				throw new UnsupportedOperationException();
			}

			@Nonnull
			@Override
			public Collection<? extends Model> getMultiple() {
				return Collections.emptyList();
			}
		};

		return new ChildSupplier() {
			@Override
			public boolean isSingle() {
				return true;
			}

			@Nonnull
			@Override
			public Model getSingle() {
				return model;
			}

			@Nonnull
			@Override
			public Collection<? extends Model> getMultiple() {
				return Collections.emptyList();
			}
		};
	}

	@Nonnull
	static ChildSupplier of(@Nonnull Collection<? extends Model> models) {
		return new ChildSupplier() {
			@Override
			public boolean isSingle() {
				return false;
			}

			@Nonnull
			@Override
			public Model getSingle() {
				throw new UnsupportedOperationException();
			}

			@Nonnull
			@Override
			public Collection<? extends Model> getMultiple() {
				return models;
			}
		};
	}

	@Nonnull
	static ChildSupplier of(Model... models) {
		return new ChildSupplier() {
			@Override
			public boolean isSingle() {
				return false;
			}

			@Nonnull
			@Override
			public Model getSingle() {
				throw new UnsupportedOperationException();
			}

			@Nonnull
			@Override
			public Collection<? extends Model> getMultiple() {
				return Arrays.asList(models);
			}
		};
	}

	/**
	 * @return {@code true} for when {@link #getSingle()} should be used.
	 * {@code false} for when {@link #getMultiple()} should be used.
	 */
	boolean isSingle();

	/**
	 * @return Single supplied child model.
	 */
	@Nonnull
	Model getSingle();

	/**
	 * @return Multiple supplied child models.
	 */
	@Nonnull
	Collection<? extends Model> getMultiple();
}
