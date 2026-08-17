import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	ArrayList<String> arrayList = new ArrayList<>();
	MutableList<String> list;

	void test()
	{
		DoubleSummaryStatistics result1 = arrayList.stream().collect(Collectors.summarizingDouble(String::length));
		List<String> result2 = list.stream().collect(Collectors.toList());
	}
}
