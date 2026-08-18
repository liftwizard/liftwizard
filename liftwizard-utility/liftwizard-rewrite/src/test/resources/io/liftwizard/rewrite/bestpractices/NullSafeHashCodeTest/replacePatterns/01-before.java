class Test
{
	int test(String str, Integer num)
	{
		int hash1 = str == null ? 0 : str.hashCode();
		int hash2 = num == null ? 0 : num.hashCode();
		int hash3 = str != null ? str.hashCode() : 0;
		int hash4 = num != null ? num.hashCode() : 0;

		return str == null ? 0 : str.hashCode();
	}
}
