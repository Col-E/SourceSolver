package software.coley.sourcesolver.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public interface ChildSupplier {
	@Nonnull
	static ChildSupplier of(@Nullable AbstractModel model) {
		if (model == null) return new ChildSupplier() {
			@Override
			public boolean isSingle() {
				return false;
			}

			@Override
			public AbstractModel getSingle() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Collection<? extends AbstractModel> getMultiple() {
				return Collections.emptyList();
			}
		};

		return new ChildSupplier() {
			@Override
			public boolean isSingle() {
				return true;
			}

			@Override
			public AbstractModel getSingle() {
				return model;
			}

			@Override
			public Collection<? extends AbstractModel> getMultiple() {
				return Collections.emptyList();
			}
		};
	}

	@Nonnull
	static ChildSupplier of(@Nonnull Collection<? extends AbstractModel> models) {
		return new ChildSupplier() {
			@Override
			public boolean isSingle() {
				return false;
			}

			@Override
			public AbstractModel getSingle() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Collection<? extends AbstractModel> getMultiple() {
				return models;
			}
		};
	}

	@Nonnull
	static ChildSupplier of(AbstractModel... models) {
		return new ChildSupplier() {
			@Override
			public boolean isSingle() {
				return false;
			}

			@Override
			public AbstractModel getSingle() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Collection<? extends AbstractModel> getMultiple() {
				return Arrays.asList(models);
			}
		};
	}

	boolean isSingle();

	AbstractModel getSingle();

	Collection<? extends AbstractModel> getMultiple();
}
