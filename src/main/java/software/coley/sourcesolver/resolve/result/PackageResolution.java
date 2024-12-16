package software.coley.sourcesolver.resolve.result;

import javax.annotation.Nullable;

non-sealed public interface PackageResolution extends Resolution {
	/**
	 * @return Package name in internal format, or {@code null} for the default package.
	 */
	@Nullable
	String getPackageName();

	default boolean isDefaultPackage() {
		return getPackageName() == null;
	}
}
