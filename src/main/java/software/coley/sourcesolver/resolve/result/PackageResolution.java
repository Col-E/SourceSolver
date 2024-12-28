package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nullable;

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
}
