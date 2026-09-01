package example;

import java.util.List;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

class ImplFactoryListsAlreadyImported
{
	void test(List<String> input)
	{
		MutableList<String> adapted = Lists.adapt(input);
		List<String> emptyList = Lists.fixedSize.empty();
	}
}
