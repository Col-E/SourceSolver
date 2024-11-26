package software.coley.sourcesolver.resolve;

public sealed interface Resolution {
	non-sealed interface TypeResolution extends Resolution {
		String type();
	}

	sealed interface MemberResolution extends Resolution {
		TypeResolution owner();

		String name();

		String descriptor();
	}

	non-sealed interface FieldResolution extends MemberResolution {}
	non-sealed interface MethodResolution extends MemberResolution {}
}
