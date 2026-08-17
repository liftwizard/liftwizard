import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.test.Verify;

class Test
{
	void test()
	{
		Integer[] numbers = new Integer[]
		{
			1,
			2,
			3,
			4,
			5,
		};
		Verify.assertSize("numbers should have expected size", 5, numbers);
		Verify.assertSize(5, numbers);

		Object[] objects = new Object[10];
		Verify.assertSize("objects should have expected size", 10, objects);
		Verify.assertSize(10, objects);

		Integer[] emptyArray = new Integer[0];
		Verify.assertSize("should be empty", 0, emptyArray);
		Verify.assertSize(0, emptyArray);

		MutableList<Integer> mutableNumbers = Lists.mutable.with(1, 2, 3, 4, 5);
		Verify.assertSize("numbers should have expected size", 5, mutableNumbers);
		Verify.assertSize(5, mutableNumbers);

		MutableList<Integer> emptyList = Lists.mutable.with();
		Verify.assertSize("should be empty", 0, emptyList);
		Verify.assertSize(0, emptyList);

		List<? extends Number> boundedWildcard = new ArrayList<Integer>();
		((ArrayList<Integer>) boundedWildcard).add(1);
		((ArrayList<Integer>) boundedWildcard).add(2);
		Verify.assertSize("bounded wildcard list should have size 2", 2, boundedWildcard);
		Verify.assertSize(2, boundedWildcard);

		List<? super Integer> lowerBoundedWildcard = new ArrayList<Number>();
		lowerBoundedWildcard.add(1);
		lowerBoundedWildcard.add(2);
		lowerBoundedWildcard.add(3);
		Verify.assertSize(3, lowerBoundedWildcard);

		List rawType = new ArrayList();
		rawType.add("element");
		Verify.assertSize("raw type list should have size 1", 1, rawType);
		Verify.assertSize(1, rawType);

		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Verify.assertSize("map should have size 2", 2, map);
		Verify.assertSize(2, map);

		Map<String, Integer> emptyMap = new HashMap<>();
		Verify.assertSize("map should be empty", 0, emptyMap);
		Verify.assertSize(0, emptyMap);
	}
}
