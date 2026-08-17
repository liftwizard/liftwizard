import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> mutableList;
	List<String> jcfList;

	// No delimiter - Collectors.joining() uses "" as default, makeString() uses ", "
	String invalid1 = mutableList.stream().collect(Collectors.joining());

	// Different map function (not toString)
	String invalid2 = mutableList.stream().map(String::toUpperCase).collect(Collectors.joining(", "));

	// Three-arg joining (with prefix and suffix)
	String invalid3 = mutableList.stream().collect(Collectors.joining(", ", "[", "]"));

	// Non-Eclipse Collections type (JCF List)
	String invalid4 = jcfList.stream().collect(Collectors.joining(", "));

	// Intermediate operations (filter before collect)
	String invalid5()
	{
		return mutableList
			.stream()
			.filter((s) -> !s.isEmpty())
			.collect(Collectors.joining(", "));
	}
}
