import java.util.List;
import java.util.Map;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

class Test
{
	private final Map<String, Integer> fieldMap = Maps.mutable.empty();
	private final java.util.Map<String, Integer> fullyQualifiedField = Maps.mutable.with("a", 1);

	void test()
	{
		Map<String, Integer> map = Maps.mutable.empty();
		java.util.Map<String, Integer> fullyQualified = Maps.mutable.empty();
		Map rawMap = Maps.mutable.empty();
		java.util.Map rawMapFullyQualified = Maps.mutable.empty();
		Map<String, List<Integer>> nestedGenerics = Maps.mutable.empty();
		Map<String, Integer> unifiedMap = UnifiedMap.newMap();
		Map<String, Integer> map1 = Maps.mutable.empty(),
			map2 = Maps.mutable.with("a", 1);
	}
}

class ConstructorExample
{
	private final Map<String, Integer> fieldMap;

	ConstructorExample()
	{
		Map<String, Integer> localMap = Maps.mutable.empty();
		this.fieldMap = localMap;
	}
}

class WildcardExample
{
	void test()
	{
		Map<? extends String, ? extends Integer> extendsMap = Maps.mutable.empty();
		Map<? super String, ? super Integer> superMap = Maps.mutable.empty();
		Map<?, ?> unboundedMap = Maps.mutable.empty();
	}
}
