package software.coley.sourcesolver.resolve.entry;

import java.lang.reflect.Modifier;

/**
 * Metadata model of an item with access modifiers.
 *
 * @author Matt Coley
 */
public interface AccessedEntry {
	/**
	 * @return Access modifiers of this entry.
	 */
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

	default boolean isStatic() {
		return (getAccess() & Modifier.STATIC) != 0;
	}
}
