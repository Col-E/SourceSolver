package sample;

public class OuterClass {
	private final OuterClass.InnerClass inner1 = new InnerClass();
	private final InnerClass inner2 = new OuterClass.InnerClass();

	public static class InnerClass {
		public String example = "Hello";
	}

	void main() {
		InnerClass inner = Math.random() > 0.5 ? inner1 : inner2;
		System.out.println(inner.example);
	}
}
