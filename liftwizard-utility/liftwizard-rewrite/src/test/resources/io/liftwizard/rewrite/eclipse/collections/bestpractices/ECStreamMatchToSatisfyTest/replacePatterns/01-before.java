import java.util.function.Predicate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	boolean anyMatchToAnySatisfy(MutableList<String> list, Predicate<String> predicate)
	{
		return list.stream().anyMatch(predicate);
	}

	boolean allMatchToAllSatisfy(MutableList<String> list, Predicate<String> predicate)
	{
		return list.stream().allMatch(predicate);
	}

	boolean noneMatchToNoneSatisfy(MutableList<String> list, Predicate<String> predicate)
	{
		return list.stream().noneMatch(predicate);
	}

	void withLambdaPredicate(MutableList<String> list)
	{
		boolean anyLong = list.stream().anyMatch((s) -> s.length() > 5);
		boolean allLong = list.stream().allMatch((s) -> s.length() > 5);
		boolean noneLong = list.stream().noneMatch((s) -> s.length() > 5);
	}

	void withMethodReference(MutableList<String> list)
	{
		boolean anyEmpty = list.stream().anyMatch(String::isEmpty);
		boolean allEmpty = list.stream().allMatch(String::isEmpty);
		boolean noneEmpty = list.stream().noneMatch(String::isEmpty);
	}

	boolean withImmutableList(ImmutableList<String> list, Predicate<String> predicate)
	{
		return list.stream().anyMatch(predicate);
	}

	boolean withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate)
	{
		return set.stream().anyMatch(predicate);
	}

	void inIfCondition(MutableList<String> list)
	{
		if (list.stream().anyMatch(String::isEmpty))
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
