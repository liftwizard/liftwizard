import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.set.mutable.SetAdapter;

public class Example
{
	void setsUnion(Set<String> a, Set<String> b)
	{
		Set<String> union = Sets.union(a, b);
	}

	void listsAdapt()
	{
		List<String> javaList = new ArrayList<>();
		MutableList<String> adapted = Lists.adapt(javaList);
	}

	void mapsFactoryAndUtility()
	{
		MutableMap<Integer, Integer> map = Maps.mutable.with(1, 1, 2, 2, 3, 3);
		MutableMap<String, String> adapted = Maps.adapt(new HashMap<>());
	}

	void setsFactoryAndUtilityMixed()
	{
		MutableSet<Integer> adapter1 = SetAdapter.adapt(Sets.fixedSize.of(1, 2, 3, 4));
		MutableSet<Integer> adapter2 = Sets.adapt(Sets.fixedSize.of(1, 2, 3, 4));
	}
}
