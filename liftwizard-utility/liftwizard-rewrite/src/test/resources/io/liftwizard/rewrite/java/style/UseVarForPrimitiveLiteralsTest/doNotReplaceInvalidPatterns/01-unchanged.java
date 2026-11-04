class Test
{
	// Field declarations - var not allowed
	private int fieldInt = 42;
	private String fieldStr = "hello";

	void test()
	{
		// Method return - type not obvious from literal
		int fromMethod = Integer.parseInt("42");
		boolean fromMethodBool = "hello".isEmpty();
		String fromMethodStr = String.valueOf(42);

		// byte - looks like int literal
		byte b = 42;

		// short - looks like int literal
		short s = 42;

		// char literal widened to a numeric type - var would make it a char
		int charToInt = 'a';
		long charToLong = 'a';
		double charToDouble = 'a';

		// hex, octal, and binary int literals - an F or D suffix changes their value or does not compile
		double hexToDouble = 0x10;
		float hexToFloat = 0x10;
		double octalToDouble = 010;
		double binaryToDouble = 0b10;

		// long and float literals widened to a floating-point type - var would keep the narrower type
		float longToFloat = 1L;
		double longToDouble = 10L;
		double floatToDouble = 1.5f;

		// literal assigned to a supertype or boxed type - var would narrow it to the literal's type
		Object objectFromString = "hello";
		Integer boxed = 42;

		// Already using var
		var existing = 42;

		// No initializer
		int noInit;

		// Multiple variables
		int a = 1,
			x = 2;

		// Null initializer
		String nullInit = null;

		// Ternary initializer (still a literal check, but ternary is not a literal)
		int ternary = true ? 1 : 2;

		// Constructor call (not a literal)
		StringBuilder sb = new StringBuilder();
	}
}
