import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Bags;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.factory.SortedBags;
import org.eclipse.collections.impl.factory.SortedMaps;
import org.eclipse.collections.impl.factory.SortedSets;
import org.eclipse.collections.impl.factory.Stacks;

public class Example
{
	private MutableList<String> listField = Lists.mutable.empty();
	private final MutableSet<String> setField;

	public Example()
	{
		this.setField = Sets.mutable.empty();
	}

	void method()
	{
		var list = Lists.mutable.empty();
		var set = Sets.mutable.empty();
		var map = Maps.mutable.empty();
		var bag = Bags.mutable.empty();
		var stack = Stacks.mutable.empty();
		var sortedSet = SortedSets.mutable.empty();
		var sortedMap = SortedMaps.mutable.empty();
		var sortedBag = SortedBags.mutable.empty();
	}
}
