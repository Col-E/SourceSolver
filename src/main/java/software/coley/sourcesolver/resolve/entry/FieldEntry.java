package software.coley.sourcesolver.resolve.entry;

public interface FieldEntry extends MemberEntry {
	default boolean isField() {
		return true;
	}

	default boolean isMethod() {
		return false;
	}
}
