import java.util.Objects;

class Test
{
	int test(String str, Integer num)
	{
		int hash1 = Objects.hashCode(str);
		int hash2 = Objects.hashCode(num);
		int hash3 = Objects.hashCode(str);
		int hash4 = Objects.hashCode(num);

		return Objects.hashCode(str);
	}
}
