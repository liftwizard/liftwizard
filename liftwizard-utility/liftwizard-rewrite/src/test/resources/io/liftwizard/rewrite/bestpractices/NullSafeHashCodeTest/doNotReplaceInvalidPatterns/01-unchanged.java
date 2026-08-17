class Test
{
	void test(String str)
	{
		int simpleHashCode = str.hashCode();
		int simpleNullCheck = str == null ? 0 : 1;
		int differentValue = str == null ? 1 : str.hashCode();
		int invertedDifferentValue = str != null ? str.hashCode() : 1;
	}
}
