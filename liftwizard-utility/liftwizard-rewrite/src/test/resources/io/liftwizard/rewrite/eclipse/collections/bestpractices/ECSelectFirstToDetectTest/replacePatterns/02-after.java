import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;

class TestGetFirst
{
	String test(MutableList<String> list, Predicate<String> predicate)
	{
		return list.detect(predicate);
	}
}
