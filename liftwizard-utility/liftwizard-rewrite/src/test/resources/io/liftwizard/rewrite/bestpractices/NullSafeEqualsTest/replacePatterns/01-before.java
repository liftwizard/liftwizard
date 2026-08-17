class Test
{
	void test(String left, String right, Integer a, Integer b)
	{
		boolean notEqualsPattern1 = left == null ? right != null : !left.equals(right);
		boolean notEqualsPattern2 = right == null ? left != null : !right.equals(left);
		boolean equalsPattern1 = left == null ? right == null : left.equals(right);
		boolean equalsPattern2 = right == null ? left == null : right.equals(left);
		boolean equalsPattern3 = left == null ? right == null : left == right || left.equals(right);
		boolean equalsPattern4 = left == right || (left != null && left.equals(right));
		boolean equalsPattern5 = right == left || (left != null && left.equals(right));
		boolean equalsPattern6 = left == null || right == null ? left == right : left.equals(right);
		boolean differentTypes = a == null ? b == null : a.equals(b);
	}
}
