import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Test
{
	private final List<String> fieldSingletonList = Collections.singletonList("element");
	private final Set<String> fieldSingleton = Collections.singleton("element");
	private final Map<String, String> fieldSingletonMap = Collections.singletonMap("key", "value");

	void test()
	{
		List<String> singletonList = Collections.singletonList("element");
		Set<String> singleton = Collections.singleton("element");
		Map<String, String> singletonMap = Collections.singletonMap("key", "value");
	}

	void testWithExplicitGenerics(Object element, Object key, Object value)
	{
		List<String> singletonList = Collections.<String>singletonList((String) element);
		Set<String> singleton = Collections.<String>singleton((String) element);
		Map<String, String> singletonMap = Collections.<String, String>singletonMap((String) key, (String) value);
	}
}
