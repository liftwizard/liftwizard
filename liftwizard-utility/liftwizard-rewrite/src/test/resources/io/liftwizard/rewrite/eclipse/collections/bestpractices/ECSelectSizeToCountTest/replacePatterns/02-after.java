import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.ImmutableList;

class TestImmutable
{
	int test(ImmutableList<String> list, Predicate<String> predicate)
	{
		return list.count(predicate);
	}
}
