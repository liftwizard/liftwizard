import java.util.stream.Stream;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void withIntermediateOperations(MutableList<String> list)
	{
		list.stream()
			.filter((s) -> s.length() > 5)
			.forEach(System.out::println);
	}

	Stream<String> onlyStream(MutableList<String> list)
	{
		return list.stream();
	}
}
