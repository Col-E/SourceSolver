package software.coley.sourcesolver.resolve.entry;

public interface MethodEntry extends MemberEntry {
	default boolean isField() {
		return false;
	}

	default boolean isMethod() {
		return true;
	}
}
