import java.util.Map;
import org.eclipse.collections.impl.utility.MapIterate;

class Test
{
	boolean testIsEmpty(Map<String, Integer> map)
	{
		return map == null || map.isEmpty();
	}

	boolean testNotEmpty(Map<String, Integer> map)
	{
		return map != null && !map.isEmpty();
	}

	boolean testNegatedMapIterateIsEmpty(Map<String, Integer> map)
	{
		return !MapIterate.isEmpty(map);
	}

	boolean testNegatedMapIterateNotEmpty(Map<String, Integer> map)
	{
		return !MapIterate.notEmpty(map);
	}

	void testMultiple(Map<String, Integer> map1, Map<String, Object> map2)
	{
		if (map1 == null || map1.isEmpty())
		{
		}

		if (map2 != null && !map2.isEmpty())
		{
		}

		if (!MapIterate.isEmpty(map1))
		{
		}

		if (!MapIterate.notEmpty(map2))
		{
		}
	}
}
