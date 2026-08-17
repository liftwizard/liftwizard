import java.util.Optional;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	Optional<String> test(MutableList<String> list, Predicate<String> predicate)
	{
		return list.select(predicate).getFirstOptional();
	}
}
