import java.util.List;
import java.util.Map;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

class Test
{
	private final MutableMap<String, Integer> fieldMap = Maps.mutable.empty();
	private final MutableMap<String, Integer> fullyQualifiedField = Maps.mutable.with("a", 1);

	void test()
	{
		MutableMap<String, Integer> map = Maps.mutable.empty();
		MutableMap<String, Integer> fullyQualified = Maps.mutable.empty();
		MutableMap rawMap = Maps.mutable.empty();
		MutableMap rawMapFullyQualified = Maps.mutable.empty();
		MutableMap<String, List<Integer>> nestedGenerics = Maps.mutable.empty();
		MutableMap<String, Integer> unifiedMap = UnifiedMap.newMap();
		MutableMap<String, Integer> map1 = Maps.mutable.empty(),
			map2 = Maps.mutable.with("a", 1);
	}
}

class ConstructorExample
{
	private final Map<String, Integer> fieldMap;

	ConstructorExample()
	{
		MutableMap<String, Integer> localMap = Maps.mutable.empty();
		this.fieldMap = localMap;
	}
}

class WildcardExample
{
	void test()
	{
		MutableMap<? extends String, ? extends Integer> extendsMap = Maps.mutable.empty();
		MutableMap<? super String, ? super Integer> superMap = Maps.mutable.empty();
		MutableMap<?, ?> unboundedMap = Maps.mutable.empty();
	}
}
