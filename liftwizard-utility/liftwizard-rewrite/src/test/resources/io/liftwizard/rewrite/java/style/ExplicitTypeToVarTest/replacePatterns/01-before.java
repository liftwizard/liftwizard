import java.util.ArrayList;
import java.util.HashMap;

class Test<E, K, V>
{
	void test()
	{
		// Basic constructor
		StringBuilder sb = new StringBuilder();

		// Constructor with arguments
		StringBuilder sbWithArg = new StringBuilder("initial");

		// Generics with concrete types
		ArrayList<String> list = new ArrayList<>();

		// Nested generics with concrete types
		HashMap<String, ArrayList<Integer>> map = new HashMap<>();

		// Type variable in generic
		ArrayList<E> typeVarList = new ArrayList<>();

		// Multiple type variables
		HashMap<K, V> typeVarMap = new HashMap<>();

		// Nested type variables
		HashMap<K, ArrayList<V>> nested = new HashMap<>();

		// In lambda
		Runnable r = () ->
		{
			ArrayList<String> lambdaList = new ArrayList<>();
		};
	}

	// Instance initializer
	{
		StringBuilder initSb = new StringBuilder();
	}

	// Static initializer
	static
	{
		StringBuilder staticSb = new StringBuilder();
	}
}
