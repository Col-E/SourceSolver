package sample;

public class OuterClass {
	public static class InnerClass {
		public String example = "Hello";
	}

	void main() {
		System.out.println(new InnerClass().example);
	}
}
