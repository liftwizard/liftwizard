import java.util.List;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.ListIterate;

class TestListIterate
{
	boolean test(List<String> list, Predicate<String> predicate)
	{
		return ListIterate.select(list, predicate).notEmpty();
	}
}
