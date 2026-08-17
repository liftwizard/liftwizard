import java.util.List;
import java.util.Set;
import org.eclipse.collections.impl.utility.Iterate;

class Test
{
	boolean testIsEmpty(List<String> list)
	{
		return Iterate.isEmpty(list);
	}

	boolean testNotEmpty(Set<Integer> set)
	{
		return Iterate.notEmpty(set);
	}

	boolean testNegatedIterateIsEmpty(List<String> list)
	{
		return Iterate.notEmpty(list);
	}

	boolean testNegatedIterateNotEmpty(List<String> list)
	{
		return Iterate.isEmpty(list);
	}

	void testMultiple(List<String> strings, Set<Object> objects)
	{
		if (Iterate.isEmpty(strings))
		{
		}

		if (Iterate.notEmpty(objects))
		{
		}

		if (Iterate.notEmpty(strings))
		{
		}

		if (Iterate.isEmpty(objects))
		{
		}
	}
}
