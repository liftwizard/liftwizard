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
		DoubleSummaryStatistics result1 = mutableList.collectDouble(String::length).summaryStatistics();
		DoubleSummaryStatistics result2 = mutableList
			.collectDouble((s) -> s.length() * 1.0)
			.summaryStatistics();
		IntSummaryStatistics result3 = mutableList.collectInt(String::length).summaryStatistics();
		LongSummaryStatistics result4 = mutableList
			.collectLong((s) -> (long) s.length())
			.summaryStatistics();
		DoubleSummaryStatistics result5 = immutableList.collectDouble(String::length).summaryStatistics();
		IntSummaryStatistics result6 = set.collectInt(String::length).summaryStatistics();
	}
}
