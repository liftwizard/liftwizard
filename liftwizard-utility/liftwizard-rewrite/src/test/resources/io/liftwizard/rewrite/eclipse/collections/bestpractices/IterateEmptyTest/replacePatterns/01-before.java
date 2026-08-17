import java.util.List;
import java.util.Set;
import org.eclipse.collections.impl.utility.Iterate;

class Test
{
	boolean testIsEmpty(List<String> list)
	{
		return list == null || list.isEmpty();
	}

	boolean testNotEmpty(Set<Integer> set)
	{
		return set != null && !set.isEmpty();
	}

	boolean testNegatedIterateIsEmpty(List<String> list)
	{
		return !Iterate.isEmpty(list);
	}

	boolean testNegatedIterateNotEmpty(List<String> list)
	{
		return !Iterate.notEmpty(list);
	}

	void testMultiple(List<String> strings, Set<Object> objects)
	{
		if (strings == null || strings.isEmpty())
		{
		}

		if (objects != null && !objects.isEmpty())
		{
		}

		if (!Iterate.isEmpty(strings))
		{
		}

		if (!Iterate.notEmpty(objects))
		{
		}
	}
}
