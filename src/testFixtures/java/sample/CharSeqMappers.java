package sample;

public class CharSeqMappers {
	private static final CharSeqMapper<String, Integer> toInt = new CharSeqMapper<>() {
		@Override
		public Integer map(String c) {
			return Integer.parseInt(c);
		}

		@Override
		public Integer map(String c1, String c2) {
			return Integer.parseInt(c1 + c2);
		}

		@Override
		public Integer map(String... strings) {
			return Integer.parseInt(String.join("", strings));
		}
	};
}
