import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> strings;
	MutableList<StringBuilder> builders;
	MutableList<Entry<String, Integer>> entries;
	RichIterable<String> richIterable;
	String prefix;
	String target;
	String suffix;
	String text;
	Integer value;

	void all()
	{
		strings.selectWith(String::startsWith, prefix);
		strings.selectWith(Object::equals, target);
		strings.selectWith(String::startsWith, prefix);
		richIterable.selectWith(String::startsWith, prefix);
		strings.rejectWith(String::startsWith, prefix);
		strings.collectWith(String::concat, suffix);
		strings.detectWith(String::startsWith, prefix);
		strings.detectWithOptional(String::startsWith, prefix);
		strings.detectWithIfNone(String::startsWith, prefix, () -> "fallback");
		strings.anySatisfyWith(String::startsWith, prefix);
		strings.allSatisfyWith(String::startsWith, prefix);
		strings.noneSatisfyWith(String::startsWith, prefix);
		strings.countWith(String::startsWith, prefix);
		strings.partitionWith(String::startsWith, prefix);
		strings.countByWith(String::concat, suffix);
		builders.forEachWith(StringBuilder::append, text);
		strings.removeIfWith(String::startsWith, prefix);
		entries.anySatisfyWith(Object::equals, value);
		entries.forEachWith(Entry::setValue, value);
		entries.forEachWith(Entry::setValue, value);
	}
}
