import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.factory.Sets;

class Test
{
	Set<String> methodReturnType()
	{
		return Sets.mutable.empty();
	}

	void methodParameter(Set<String> set)
	{
		set.size();
	}

	void variableWithNonMutableSetInitializer()
	{
		Set<String> hashSet = new HashSet<>();
	}

	void variableWithoutInitializer()
	{
		Set<String> uninitializedSet;
	}

	void nonFinalField()
	{
		class Inner
		{
			private Set<String> nonFinalField = Sets.mutable.empty();
		}
	}
}

interface MyInterface
	extends Set<String>
{
}

class ImplementsExample
	implements Set<String>
{
}

class GenericBoundsExample<T extends Set<String>>
{
}
