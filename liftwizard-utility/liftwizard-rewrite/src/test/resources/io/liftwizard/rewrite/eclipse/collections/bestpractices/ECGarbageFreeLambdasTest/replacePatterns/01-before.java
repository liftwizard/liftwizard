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
		strings.select((s) -> s.startsWith(prefix));
		strings.select((s) -> s.equals(target));
		strings.select((s) ->
		{
			return s.startsWith(prefix);
		});
		richIterable.select((s) -> s.startsWith(prefix));
		strings.reject((s) -> s.startsWith(prefix));
		strings.collect((s) -> s.concat(suffix));
		strings.detect((s) -> s.startsWith(prefix));
		strings.detectOptional((s) -> s.startsWith(prefix));
		strings.detectIfNone((s) -> s.startsWith(prefix), () -> "fallback");
		strings.anySatisfy((s) -> s.startsWith(prefix));
		strings.allSatisfy((s) -> s.startsWith(prefix));
		strings.noneSatisfy((s) -> s.startsWith(prefix));
		strings.count((s) -> s.startsWith(prefix));
		strings.partition((s) -> s.startsWith(prefix));
		strings.countBy((s) -> s.concat(suffix));
		builders.forEach((b) -> b.append(text));
		strings.removeIf((s) -> s.startsWith(prefix));
		entries.anySatisfy((e) -> e.equals(value));
		entries.forEach((e) -> e.setValue(value));
		entries.forEach((entry) -> entry.setValue(value));
	}
}
