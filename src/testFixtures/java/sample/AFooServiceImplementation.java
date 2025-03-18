package sample;

public class AFooServiceImplementation extends AbstractFooService {
	@Override
	public void foo() {
		super.foo();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	class Bar {
		void bar() {
			AFooServiceImplementation.super.finalFoo();
		}
	}
}
