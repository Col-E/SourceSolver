package sample;

@SuppressWarnings("unused")
public class StaticInitBacked {
	private static int value;

	static {
		value = 1;
	}
}
