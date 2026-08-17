import java.util.ArrayList;
import java.util.List;
import org.eclipse.collections.api.factory.Lists;

class Test
{
	List<String> methodReturnType()
	{
		return Lists.mutable.empty();
	}

	void methodParameter(List<String> list)
	{
		list.size();
	}

	void variableWithNonMutableListInitializer()
	{
		List<String> arrayList = new ArrayList<>();
	}

	void variableWithoutInitializer()
	{
		List<String> uninitializedList;
	}

	void nonFinalField()
	{
		class Inner
		{
			private List<String> nonFinalField = Lists.mutable.empty();
		}
	}
}

interface MyInterface
	extends List<String>
{
}

class ImplementsExample
	implements List<String>
{
}

class GenericBoundsExample<T extends List<String>>
{
}
