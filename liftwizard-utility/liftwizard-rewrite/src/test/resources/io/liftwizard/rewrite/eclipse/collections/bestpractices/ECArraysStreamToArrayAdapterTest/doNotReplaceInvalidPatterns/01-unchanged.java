import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Test {
	void primitiveAndRangedStreams(
		int[] intValues,
		long[] longValues,
		double[] doubleValues,
		String[] values
	) {
		var result1 = Arrays.stream(intValues).sum();
		var result2 = Arrays.stream(longValues).sum();
		var result3 = Arrays.stream(doubleValues).sum();
		var result4 = Arrays.stream(values, 1, 3).toList();
	}

	void untranslatableChains(String[] values, Predicate<String> predicate, long n) {
		var result1 = Arrays.stream(values).collect(Collectors.groupingBy(String::length));
		var result2 = Arrays.stream(values).count();
		var result3 = Arrays.stream(values).toArray(String[]::new);
		var result4 = Arrays.stream(values).filter(predicate).toList();
		var result5 = Arrays.stream(values).skip(n).toArray();
		var result6 = Arrays.stream(values).findFirst();
		var result7 = Arrays.stream(values).skip(1);
	}

	Stream<String> streamTypedUsages(String[] values) {
		Stream<String> stream = Arrays.stream(values);
		this.consume(Arrays.stream(values));
		return Arrays.stream(values);
	}

	void consume(Stream<String> stream) {
	}
}
