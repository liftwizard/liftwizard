import java.util.stream.IntStream;

class Test {
	void test() {
		IntStream.range(1, 10).forEach(System.out::println);
		IntStream stream = IntStream.of(1, 2, 3);
	}
}
