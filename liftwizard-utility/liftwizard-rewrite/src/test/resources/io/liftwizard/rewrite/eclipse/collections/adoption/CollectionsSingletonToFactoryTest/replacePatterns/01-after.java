import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;

class Test
{
	private final List<String> fieldSingletonList = Lists.fixedSize.with("element");
	private final Set<String> fieldSingleton = Sets.fixedSize.with("element");
	private final Map<String, String> fieldSingletonMap = Maps.fixedSize.with("key", "value");

	void test()
	{
		List<String> singletonList = Lists.fixedSize.with("element");
		Set<String> singleton = Sets.fixedSize.with("element");
		Map<String, String> singletonMap = Maps.fixedSize.with("key", "value");
	}

	void testWithExplicitGenerics(Object element, Object key, Object value)
	{
		List<String> singletonList = Lists.fixedSize.with((String) element);
		Set<String> singleton = Sets.fixedSize.with((String) element);
		Map<String, String> singletonMap = Maps.fixedSize.with((String) key, (String) value);
	}
}
