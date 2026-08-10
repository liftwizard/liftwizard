import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;

class Test {
	MutableList<String> strings;
	MutableList<StringBuilder> builders;
	Predicate<String> predicate;
	Function<String, String> fn;
	String prefix;

	void all() {
		strings.select(s -> s.startsWith(s));
		strings.select(s -> s.isEmpty());
		strings.select(s -> s.trim().startsWith(prefix));
		strings.select(String::isEmpty);
		strings.select(predicate);
		strings.select(s -> s.regionMatches(0, prefix, 0, 1));
		strings.collect(fn);
		strings.collect(String::trim);
		strings.detectIfNone(s -> s.startsWith(s), () -> "");
		strings.detectIfNone(String::isEmpty, () -> "");
		builders.forEach(b -> b.append(b.toString()));
		builders.forEach(StringBuilder::reverse);
	}
}
