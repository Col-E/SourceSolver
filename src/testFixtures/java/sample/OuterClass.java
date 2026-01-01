package sample;

public class OuterClass {
	private final OuterClass.InnerClass inner1 = new InnerClass();
	private final InnerClass inner2 = new OuterClass.InnerClass();

	public class InnerClass {
		public final String example;

		private InnerClass() {
			this("hello");
		}

		private InnerClass(String message) {
			this.example = message;
		}

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

	String outerIntoInner(Box<String> box) {
		return new InnerClass() {
			@Override
			String getExample() {
				return box.value;
			}
		}.getExample();
	}
}
