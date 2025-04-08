package sample;

import jakarta.annotation.Nonnull;

public class AnnotationsEverywhere {
	@Nonnull public static @AnnoAnywhere String upper(@AnnoAnywhere String foo) {
		@AnnoAnywhere
		String up = foo.toLowerCase();
		return up;
	}

	public static String @AnnoAnywhere [] upperArray(String @AnnoAnywhere [] foo) {
		String[] up = new String[foo.length];
		for (int i = 0; i < foo.length; i++)
			up[i] = foo[i].toUpperCase();
		return up;
	}
}
