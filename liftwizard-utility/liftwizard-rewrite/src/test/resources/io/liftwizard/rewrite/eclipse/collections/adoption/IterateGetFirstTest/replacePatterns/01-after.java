import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.impl.utility.Iterate;

class Test
{
	void testMultiplePatterns(List<String> list, ArrayList<Integer> numbers, Set<Object> set)
	{
		String listFirst = Iterate.getFirst(list);
		Integer arrayListFirst = Iterate.getFirst(numbers);
		Object setFirst = Iterate.getFirst(set);
	}
}
