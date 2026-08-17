import java.util.function.Predicate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	boolean anyMatchToAnySatisfy(MutableList<String> list, Predicate<String> predicate)
	{
		return list.anySatisfy(predicate);
	}

	boolean allMatchToAllSatisfy(MutableList<String> list, Predicate<String> predicate)
	{
		return list.allSatisfy(predicate);
	}

	boolean noneMatchToNoneSatisfy(MutableList<String> list, Predicate<String> predicate)
	{
		return list.noneSatisfy(predicate);
	}

	void withLambdaPredicate(MutableList<String> list)
	{
		boolean anyLong = list.anySatisfy((s) -> s.length() > 5);
		boolean allLong = list.allSatisfy((s) -> s.length() > 5);
		boolean noneLong = list.noneSatisfy((s) -> s.length() > 5);
	}

	void withMethodReference(MutableList<String> list)
	{
		boolean anyEmpty = list.anySatisfy(String::isEmpty);
		boolean allEmpty = list.allSatisfy(String::isEmpty);
		boolean noneEmpty = list.noneSatisfy(String::isEmpty);
	}

	boolean withImmutableList(ImmutableList<String> list, Predicate<String> predicate)
	{
		return list.anySatisfy(predicate);
	}

	boolean withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate)
	{
		return set.anySatisfy(predicate);
	}

	void inIfCondition(MutableList<String> list)
	{
		if (list.anySatisfy(String::isEmpty))
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
