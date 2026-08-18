class Test
{
	void test(String left, String right, String other, MyClass obj)
	{
		boolean simpleEquals = left.equals(right);
		boolean simpleNullCheck = left == null;
		boolean differentTernary = left == null ? false : left.equals(right);
		boolean variablesMismatch = left == null ? right == null : left.equals(other);
		boolean notEqualsMethod = left == null ? right == null : left.compareTo(right) == 0;
		boolean multipleArgs = obj == null ? right == null : obj.equals(right, true);
	}

	class MyClass
	{
		boolean equals(String other, boolean ignoreCase)
		{
			return false;
		}
	}
}
