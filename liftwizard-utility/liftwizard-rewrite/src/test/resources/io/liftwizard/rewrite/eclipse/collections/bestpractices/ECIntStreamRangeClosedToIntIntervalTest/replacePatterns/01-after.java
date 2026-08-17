import org.eclipse.collections.impl.list.primitive.IntInterval;

class Test
{
	void test(int from, int to, int n)
	{
		IntInterval.fromTo(1, 5).forEach(System.out::println);
		long result = IntInterval.fromTo(1, 100).sum();
		IntInterval.fromTo(from, to).forEach(System.out::println);
		IntInterval.fromTo(n + 1, n * 2).forEach(System.out::println);
	}
}
