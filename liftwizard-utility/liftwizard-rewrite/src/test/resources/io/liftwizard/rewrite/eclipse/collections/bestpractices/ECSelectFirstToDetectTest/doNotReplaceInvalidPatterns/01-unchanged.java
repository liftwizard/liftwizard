import java.util.Optional;
import org.eclipse.collections.api.list.MutableList;

class TestNoSelect
{
	Optional<String> test(MutableList<String> list)
	{
		return list.getFirstOptional();
	}
}
