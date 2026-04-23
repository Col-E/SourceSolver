package sample;

public enum SomeEnumeration {
	ONE, TWO, THREE;

	void foo() {}

	void bar() {
		ONE.foo();
	}
}
