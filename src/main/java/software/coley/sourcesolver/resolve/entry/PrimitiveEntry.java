package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

public interface PrimitiveEntry extends DescribableEntry {
	PrimitiveEntry VOID = new BasicPrimitiveEntry(Kind.VOID, "V");
	PrimitiveEntry BOOLEAN = new BasicPrimitiveEntry(Kind.BOOLEAN, "Z");
	PrimitiveEntry CHAR = new BasicPrimitiveEntry(Kind.CHAR, "C");
	PrimitiveEntry BYTE = new BasicPrimitiveEntry(Kind.BYTE, "B");
	PrimitiveEntry SHORT = new BasicPrimitiveEntry(Kind.SHORT, "S");
	PrimitiveEntry INT = new BasicPrimitiveEntry(Kind.INT, "I");
	PrimitiveEntry FLOAT = new BasicPrimitiveEntry(Kind.FLOAT, "F");
	PrimitiveEntry LONG = new BasicPrimitiveEntry(Kind.LONG, "J");
	PrimitiveEntry DOUBLE = new BasicPrimitiveEntry(Kind.DOUBLE, "D");

	@Nonnull
	static PrimitiveEntry getPrimitive(@Nonnull String descriptor) {
		if (descriptor.length() != 1) throw new IllegalStateException("Not a primitive descriptor: " + descriptor);
		return switch (descriptor.charAt(0)) {
			case 'Z' -> PrimitiveEntry.BOOLEAN;
			case 'B' -> PrimitiveEntry.BYTE;
			case 'S' -> PrimitiveEntry.SHORT;
			case 'I' -> PrimitiveEntry.INT;
			case 'J' -> PrimitiveEntry.LONG;
			case 'C' -> PrimitiveEntry.CHAR;
			case 'F' -> PrimitiveEntry.FLOAT;
			case 'D' -> PrimitiveEntry.DOUBLE;
			case 'V' -> PrimitiveEntry.VOID;
			default -> throw new IllegalStateException("Invalid primitive descriptor: " + descriptor);
		};
	}

	@Override
	default boolean isAssignableFrom(@Nonnull DescribableEntry other) {
		if (other instanceof PrimitiveEntry otherPrimitive)
			return isAssignableFrom(otherPrimitive);
		return false;
	}

	default boolean isAssignableFrom(@Nonnull PrimitiveEntry other) {
		return getKind().ordinal() >= other.getKind().ordinal();
	}

	@Nonnull
	Kind getKind();

	enum Kind {
		VOID,
		BOOLEAN,
		CHAR,
		BYTE,
		SHORT,
		INT,
		FLOAT,
		LONG,
		DOUBLE
	}
}