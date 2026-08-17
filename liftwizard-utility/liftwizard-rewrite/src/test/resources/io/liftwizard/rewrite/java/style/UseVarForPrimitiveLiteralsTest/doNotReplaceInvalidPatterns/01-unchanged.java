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
