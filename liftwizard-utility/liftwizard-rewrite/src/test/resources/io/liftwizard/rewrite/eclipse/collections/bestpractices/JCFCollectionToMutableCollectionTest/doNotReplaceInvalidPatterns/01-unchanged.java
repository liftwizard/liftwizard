import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.collections.api.factory.Lists;

class Test
{
	Collection<String> methodReturnType()
	{
		return Lists.mutable.empty();
	}

	void methodParameter(Collection<String> collection)
	{
		collection.size();
	}

	void variableWithNonMutableCollectionInitializer()
	{
		Collection<String> arrayList = new ArrayList<>();
	}

	void variableWithoutInitializer()
	{
		Collection<String> uninitializedCollection;
	}
}

interface MyInterface
	extends Collection<String>
{
}

class ImplementsExample
	implements Collection<String>
{
}

class GenericBoundsExample<T extends Collection<String>>
{
}
