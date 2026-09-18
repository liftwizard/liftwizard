import org.eclipse.collections.api.list.MutableList;

class TestMultiLineChain
{
	MutableList<String> strings;
	String prefix;
	String suffix;

	void all()
	{
		strings
			.asLazy()
			.selectWith(String::startsWith, prefix)
			.collectWith(String::concat, suffix)
			.toList();
	}
}
