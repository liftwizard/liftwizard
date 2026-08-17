import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<String> set;

	void test()
	{
		DoubleSummaryStatistics result1 = mutableList.stream().collect(Collectors.summarizingDouble(String::length));
		DoubleSummaryStatistics result2 = mutableList
			.stream()
			.collect(Collectors.summarizingDouble((s) -> s.length() * 1.0));
		IntSummaryStatistics result3 = mutableList.stream().collect(Collectors.summarizingInt(String::length));
		LongSummaryStatistics result4 = mutableList
			.stream()
			.collect(Collectors.summarizingLong((s) -> (long) s.length()));
		DoubleSummaryStatistics result5 = immutableList.stream().collect(Collectors.summarizingDouble(String::length));
		IntSummaryStatistics result6 = set.stream().collect(Collectors.summarizingInt(String::length));
	}
}
