import java.util.ArrayList;
import java.util.HashMap;

class Test<E, K, V>
{
	void test()
	{
		// Basic constructor
		var sb = new StringBuilder();

		// Constructor with arguments
		var sbWithArg = new StringBuilder("initial");

		// Generics with concrete types
		var list = new ArrayList<String>();

		// Nested generics with concrete types
		var map = new HashMap<String, ArrayList<Integer>>();

		// Type variable in generic
		var typeVarList = new ArrayList<E>();

		// Multiple type variables
		var typeVarMap = new HashMap<K, V>();

		// Nested type variables
		var nested = new HashMap<K, ArrayList<V>>();

		// In lambda
		Runnable r = () ->
		{
			var lambdaList = new ArrayList<String>();
		};
	}

	// Instance initializer
	{
		var initSb = new StringBuilder();
	}

	// Static initializer
	static
	{
		var staticSb = new StringBuilder();
	}
}
