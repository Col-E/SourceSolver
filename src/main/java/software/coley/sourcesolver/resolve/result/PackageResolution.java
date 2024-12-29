package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * Resolution of a package.
 */
non-sealed public interface PackageResolution extends Resolution {
	/**
	 * @return Package name in internal format, or {@code null} for the default package.
	 */
	@Nullable
	String getPackageName();

	/**
	 * @return {@code true} if the package is the default package <i>(empty name)</i>.
	 */
	default boolean isDefaultPackage() {
		return getPackageName() == null;
	}

	@Override
	default boolean matches(@Nonnull Resolution other) {
		return other instanceof PackageResolution otherPackage
				&& Objects.equals(getPackageName(), otherPackage.getPackageName());
	}
}
