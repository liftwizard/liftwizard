class Test
{
	void test()
	{
		// int literal
		int i = 42;

		// long literal with suffix
		long l1 = 42L;

		// long literal without suffix - needs L added
		long l2 = 42;

		// float literal with suffix
		float f1 = 3.14F;

		// float literal with lowercase suffix
		float f2 = 3.14f;

		// double literal with decimal point
		double d1 = 3.14;

		// double literal with suffix
		double d2 = 42D;

		// double literal without suffix or decimal - needs D added
		double d3 = 42;

		// hex and octal int literals widen to long with an L suffix
		long l3 = 0x10;
		long l4 = 010;

		// decimal int literal widens to float with an F suffix
		float f3 = 42;

		// double literal in exponent form already has type double
		double d4 = 1e3;

		// boolean literals
		boolean b1 = true;
		boolean b2 = false;

		// char literal
		char c = 'a';

		// String literal
		String s = "hello";
	}
}
