package sample;

class AltBoxUser {
	void foo(sample.alt.Box box) {}

	void foo(sample.alt.Box box, int i) {}

	void usage() {
		foo(new sample.alt.Box());
		foo(new sample.alt.Box(), 1);
	}
}