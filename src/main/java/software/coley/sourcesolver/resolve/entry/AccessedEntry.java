package software.coley.sourcesolver.resolve.entry;

import java.lang.reflect.Modifier;

public interface AccessedEntry {
	int getAccess();

	default boolean isPublic() {
		return (getAccess() & Modifier.PUBLIC) != 0;
	}

	default boolean isProtected() {
		return (getAccess() & Modifier.PROTECTED) != 0;
	}

	default boolean isPrivate() {
		return (getAccess() & Modifier.PRIVATE) != 0;
	}

	default boolean isPackageProtected() {
		return (getAccess() & (Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC)) == 0;
	}
}
