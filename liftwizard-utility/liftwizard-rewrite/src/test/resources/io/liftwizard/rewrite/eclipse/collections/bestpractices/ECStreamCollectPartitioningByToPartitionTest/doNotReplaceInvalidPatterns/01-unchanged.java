import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	ArrayList<String> arrayList = new ArrayList<>();
	MutableList<String> list;

	void test()
	{
		Map<Boolean, List<String>> result1 = arrayList
			.stream()
			.collect(Collectors.partitioningBy((s) -> s.length() > 3));
		Map<Boolean, Set<String>> result2 = list
			.stream()
			.collect(Collectors.partitioningBy((s) -> s.length() > 3, Collectors.toSet()));
	}
}
