import java.util.List;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

class Test<T>
{
	private final MutableMap<String, String> fieldInterfaceEmpty = new UnifiedMap<>();
	private final MutableMap<String, Integer> fieldInterfaceCapacity = new UnifiedMap<>(10);
	private final MutableMap<String, String> fieldInterfaceMap = new UnifiedMap<>(this.fieldInterfaceEmpty);

	void test()
	{
		MutableMap<String, Integer> diamondMap = new UnifiedMap<>();
		MutableMap<String, List<Integer>> nestedGenerics = new UnifiedMap<>();
		MutableMap<String, ?> wildcardGenerics = new UnifiedMap<>();
		MutableMap<String, ? extends Number> boundedWildcards = new UnifiedMap<>();
		MutableMap<String, Integer> explicitSimple = new UnifiedMap<String, Integer>();
		MutableMap<String, List<Integer>> explicitNested = new UnifiedMap<String, List<Integer>>();
		MutableMap<String, MutableMap<T, Integer>> nestedTypeParam = new UnifiedMap<String, MutableMap<T, Integer>>();
		org.eclipse.collections.api.map.MutableMap<String, Integer> fullyQualified =
			new org.eclipse.collections.impl.map.mutable.UnifiedMap<>();
		MutableMap<String, Integer> withCapacity = new UnifiedMap<>(16);
		MutableMap<Integer, String> withCapacity32 = new UnifiedMap<>(32);
		MutableMap<String, Integer> mapFromOther = new UnifiedMap<>(diamondMap);
	}
}

class A<K, V>
{
	@Override
	public MutableMap<K, V> newEmpty()
	{
		return new UnifiedMap<>();
	}
}
