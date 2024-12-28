package sample;

public interface CharSeqMapper<CS extends CharSequence, R> {
	R map(CS c);

	R map(CS c1, CS c2);

	@SuppressWarnings("unchecked")
	R map(CS... cs);
}
