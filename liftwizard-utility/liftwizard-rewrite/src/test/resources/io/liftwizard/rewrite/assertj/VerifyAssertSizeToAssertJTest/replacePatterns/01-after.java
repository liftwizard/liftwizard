import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

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
		Assertions.assertThat(numbers).as("numbers should have expected size").hasSize(5);
		Assertions.assertThat(numbers).hasSize(5);

		Object[] objects = new Object[10];
		Assertions.assertThat(objects).as("objects should have expected size").hasSize(10);
		Assertions.assertThat(objects).hasSize(10);

		Integer[] emptyArray = new Integer[0];
		Assertions.assertThat(emptyArray).as("should be empty").hasSize(0);
		Assertions.assertThat(emptyArray).hasSize(0);

		MutableList<Integer> mutableNumbers = Lists.mutable.with(1, 2, 3, 4, 5);
		Assertions.assertThat(mutableNumbers).as("numbers should have expected size").hasSize(5);
		Assertions.assertThat(mutableNumbers).hasSize(5);

		MutableList<Integer> emptyList = Lists.mutable.with();
		Assertions.assertThat(emptyList).as("should be empty").hasSize(0);
		Assertions.assertThat(emptyList).hasSize(0);

		List<? extends Number> boundedWildcard = new ArrayList<Integer>();
		((ArrayList<Integer>) boundedWildcard).add(1);
		((ArrayList<Integer>) boundedWildcard).add(2);
		Assertions.assertThat(boundedWildcard).as("bounded wildcard list should have size 2").hasSize(2);
		Assertions.assertThat(boundedWildcard).hasSize(2);

		List<? super Integer> lowerBoundedWildcard = new ArrayList<Number>();
		lowerBoundedWildcard.add(1);
		lowerBoundedWildcard.add(2);
		lowerBoundedWildcard.add(3);
		Assertions.assertThat(lowerBoundedWildcard).hasSize(3);

		List rawType = new ArrayList();
		rawType.add("element");
		Assertions.assertThat(rawType).as("raw type list should have size 1").hasSize(1);
		Assertions.assertThat(rawType).hasSize(1);

		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Assertions.assertThat(map).as("map should have size 2").hasSize(2);
		Assertions.assertThat(map).hasSize(2);

		Map<String, Integer> emptyMap = new HashMap<>();
		Assertions.assertThat(emptyMap).as("map should be empty").hasSize(0);
		Assertions.assertThat(emptyMap).hasSize(0);
	}
}
