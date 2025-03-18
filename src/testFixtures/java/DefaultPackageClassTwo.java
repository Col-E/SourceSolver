public class DefaultPackageClassTwo {
	private static final DefaultPackageClassTwo TWO = new DefaultPackageClassTwo();
	private final DefaultPackageClassOne one = new DefaultPackageClassOne();

	public static void main(String[] args) {
		TWO.stuff();
	}

	public void stuff() {
		stuff(one);
	}

	public static void stuff(DefaultPackageClassOne impl) {
		System.out.println(impl.toString());
	}
}
