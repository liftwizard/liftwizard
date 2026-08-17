import java.util.Map;
import org.eclipse.collections.impl.utility.MapIterate;

class Test
{
	boolean testIsEmpty(Map<String, Integer> map)
	{
		return MapIterate.isEmpty(map);
	}

	boolean testNotEmpty(Map<String, Integer> map)
	{
		return MapIterate.notEmpty(map);
	}

	boolean testNegatedMapIterateIsEmpty(Map<String, Integer> map)
	{
		return MapIterate.notEmpty(map);
	}

	boolean testNegatedMapIterateNotEmpty(Map<String, Integer> map)
	{
		return MapIterate.isEmpty(map);
	}

	void testMultiple(Map<String, Integer> map1, Map<String, Object> map2)
	{
		if (MapIterate.isEmpty(map1))
		{
		}

		if (MapIterate.notEmpty(map2))
		{
		}

		if (MapIterate.notEmpty(map1))
		{
		}

		if (MapIterate.isEmpty(map2))
		{
		}
	}
}
