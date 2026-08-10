public class AlreadyPrivateWithAssertionError {
	private AlreadyPrivateWithAssertionError() {
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}
	public static String foo() { return "foo"; }
}
