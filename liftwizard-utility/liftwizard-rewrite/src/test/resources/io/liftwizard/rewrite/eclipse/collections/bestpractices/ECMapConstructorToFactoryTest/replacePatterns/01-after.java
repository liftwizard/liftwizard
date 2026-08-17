import java.util.List;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

class Test<T>
{
	private final MutableMap<String, String> fieldInterfaceEmpty = Maps.mutable.empty();
	private final MutableMap<String, Integer> fieldInterfaceCapacity = Maps.mutable.withInitialCapacity(10);
	private final MutableMap<String, String> fieldInterfaceMap = Maps.mutable.withMap(this.fieldInterfaceEmpty);

	void test()
	{
		MutableMap<String, Integer> diamondMap = Maps.mutable.empty();
		MutableMap<String, List<Integer>> nestedGenerics = Maps.mutable.empty();
		MutableMap<String, ?> wildcardGenerics = Maps.mutable.empty();
		MutableMap<String, ? extends Number> boundedWildcards = Maps.mutable.empty();
		MutableMap<String, Integer> explicitSimple = Maps.mutable.<String, Integer>empty();
		MutableMap<String, List<Integer>> explicitNested = Maps.mutable.<String, List<Integer>>empty();
		MutableMap<String, MutableMap<T, Integer>> nestedTypeParam = Maps.mutable.<String, MutableMap<T, Integer>>empty();
		org.eclipse.collections.api.map.MutableMap<String, Integer> fullyQualified =
				Maps.mutable.empty();
		MutableMap<String, Integer> withCapacity = Maps.mutable.withInitialCapacity(16);
		MutableMap<Integer, String> withCapacity32 = Maps.mutable.withInitialCapacity(32);
		MutableMap<String, Integer> mapFromOther = Maps.mutable.withMap(diamondMap);
	}
}

class A<K, V>
{
	@Override
	public MutableMap<K, V> newEmpty()
	{
		return Maps.mutable.empty();
	}
}
