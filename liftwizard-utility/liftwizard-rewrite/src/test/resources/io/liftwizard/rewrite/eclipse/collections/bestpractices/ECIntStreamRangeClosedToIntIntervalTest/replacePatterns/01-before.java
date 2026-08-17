import java.util.stream.IntStream;

class Test
{
	void test(int from, int to, int n)
	{
		IntStream.rangeClosed(1, 5).forEach(System.out::println);
		long result = IntStream.rangeClosed(1, 100).sum();
		IntStream.rangeClosed(from, to).forEach(System.out::println);
		IntStream.rangeClosed(n + 1, n * 2).forEach(System.out::println);
	}
}
