class Test
{
	void test()
	{
		// int literal
		var i = 42;

		// long literal with suffix
		var l1 = 42L;

		// long literal without suffix - needs L added
		var l2 = 42L;

		// float literal with suffix
		var f1 = 3.14F;

		// float literal with lowercase suffix
		var f2 = 3.14f;

		// double literal with decimal point
		var d1 = 3.14;

		// double literal with suffix
		var d2 = 42D;

		// double literal without suffix or decimal - needs D added
		var d3 = 42D;

		// hex and octal int literals widen to long with an L suffix
		var l3 = 0x10L;
		var l4 = 010L;

		// decimal int literal widens to float with an F suffix
		var f3 = 42F;

		// double literal in exponent form already has type double
		var d4 = 1e3;

		// boolean literals
		var b1 = true;
		var b2 = false;

		// char literal
		var c = 'a';

		// String literal
		var s = "hello";
	}
}
