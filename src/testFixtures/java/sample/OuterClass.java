package sample;

public class OuterClass {
	private final OuterClass.InnerClass inner1 = new InnerClass();
	private final InnerClass inner2 = new OuterClass.InnerClass();

	public class InnerClass {
		public String example = "Hello";

		String getExample() {
			return example;
		}

		void innerFoo() {
			outerFoo();
		}

		@Override
		public String toString() {
			return example;
		}
	}

	void outerFoo() {
		// no-op
	}

	void main() {
		InnerClass inner = Math.random() > 0.5 ? inner1 : inner2;
		System.out.println(inner.example);
	}
}
