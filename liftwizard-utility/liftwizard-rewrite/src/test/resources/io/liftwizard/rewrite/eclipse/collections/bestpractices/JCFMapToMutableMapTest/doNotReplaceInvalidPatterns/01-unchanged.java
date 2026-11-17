import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.collections.api.factory.Maps;

class Test
{
	Map<String, Integer> getMap()
	{
		return Map.of("a", 1);
	}

	void processMap(Map<String, Integer> map)
	{
		map.get("key");
	}

	void test()
	{
		Map<String, Integer> hashMap = new HashMap<>();
		Map<String, Integer> emptyMap = Map.of();
		Map<String, Integer> mapWithEntry = Map.of("a", 1);
	}
}

class AmbiguousOverloadExample
{
	private final Map<String, Integer> assertedField = Maps.mutable.empty();

	void test()
	{
		Map<String, Integer> asserted = Maps.mutable.with("a", 1);
		assertThat(asserted).containsEntry("a", 1);
		assertThat(this.assertedField).isEmpty();
	}
}
