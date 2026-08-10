import java.util.ArrayList;
import java.util.List;

class Test {
	// Field declarations
	private final ArrayList<String> privateField = new ArrayList<>();
	protected final ArrayList<String> protectedField = new ArrayList<>();
	public final ArrayList<String> publicField = new ArrayList<>();
	final ArrayList<String> packageField = new ArrayList<>();
	ArrayList<String> nonFinalField = new ArrayList<>();

	void test() {
		// Interface vs implementation
		List<String> list = new ArrayList<>();

		// Supertype
		Object obj = new StringBuilder();

		// Already using var
		var existing = new ArrayList<>();

		// No initializer
		ArrayList<String> noInit;
		noInit = new ArrayList<>();

		// Not a constructor call
		ArrayList<String> fromFactory = this.getList();

		// Multiple variables
		String a = "a", b = "b";

		// Null initializer
		StringBuilder nullInit = null;
	}

	ArrayList<String> getList() {
		return new ArrayList<>();
	}
}
