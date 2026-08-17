import java.util.List;
import java.util.function.Predicate;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> mutableList;
	List<String> jcfList;
	Predicate<String> predicate;
	String defaultValue;

	// orElse with literal default value - would need detectIfNone
	String invalid1 = mutableList.stream().filter(predicate).findFirst().orElse("default");

	// orElse with variable default value - would need detectIfNone
	String invalid2 = mutableList.stream().filter(predicate).findFirst().orElse(defaultValue);

	// Non-Eclipse Collections type
	String invalid3 = jcfList.stream().filter(predicate).findFirst().orElse(null);

	// Without filter
	String invalid4 = mutableList.stream().findFirst().orElse(null);

	// Multiple intermediate operations
	String invalid5()
	{
		return mutableList
			.stream()
			.filter((s) -> s.length() > 3)
			.map(String::toUpperCase)
			.findFirst()
			.orElse(null);
	}
}
