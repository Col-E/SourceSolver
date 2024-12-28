package sample;

import java.util.Random;

public class Numbers {
	public static final Numbers INSTANCE = new Numbers();
	public final Integers integers = new Integers();
	public final IntArrays integerArrays = new IntArrays();

	public static class IntArrays {
		public final int[] array;

		public IntArrays() {
			this.array = new int[10];
			int last = 0;
			for (int i = 0; i < array.length; i++)
				array[i] = (last = last * 31 + 31);
		}
	}

	public static class Integers {
		public final int rand1, rand2, rand3;

		private Integers() {
			Random r = new Random();
			rand1 = r.nextInt();
			rand2 = r.nextInt();
			rand3 = r.nextInt();
		}
	}
}
