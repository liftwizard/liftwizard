import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.factory.set.MutableSetFactory;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.sets.MutableSetFactoryImpl;
import org.eclipse.collections.impl.set.mutable.SetAdapter;

public class Example
{
	// Already-api-factory imports should not change
	void alreadyApiFactory()
	{
		var list = Lists.mutable.empty();
		var set = Sets.mutable.empty();
		var map = Maps.mutable.empty();
	}

	void setsUnion(Set<String> a, Set<String> b)
	{
		Set<String> union = org.eclipse.collections.impl.factory.Sets.union(a, b);
	}

	void listsAdapt()
	{
		List<String> javaList = new ArrayList<>();
		MutableList<String> adapted = org.eclipse.collections.impl.factory.Lists.adapt(javaList);
	}

	void mapsFactoryAndUtility()
	{
		MutableMap<Integer, Integer> map = org.eclipse.collections.impl.factory.Maps.mutable.with(1, 1, 2, 2, 3, 3);
		MutableMap<String, String> adapted = org.eclipse.collections.impl.factory.Maps.adapt(new HashMap<>());
	}

	void setsFactoryAndUtilityMixed()
	{
		MutableSet<Integer> adapter1 = SetAdapter.adapt(
			org.eclipse.collections.impl.factory.Sets.fixedSize.of(1, 2, 3, 4)
		);
		MutableSet<Integer> adapter2 = org.eclipse.collections.impl.factory.Sets.adapt(
			org.eclipse.collections.impl.factory.Sets.fixedSize.of(1, 2, 3, 4)
		);
	}

	// Field initializer should not crash (Bug 1)
	public static final MutableSetFactory mutable = MutableSetFactoryImpl.INSTANCE;
}
