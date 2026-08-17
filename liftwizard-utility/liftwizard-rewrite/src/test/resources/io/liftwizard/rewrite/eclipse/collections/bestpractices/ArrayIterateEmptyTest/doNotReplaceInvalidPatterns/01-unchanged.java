class Test
{
	void test(String[] array)
	{
		boolean simpleNullCheck = array == null;
		boolean simpleLengthCheck = array.length == 0;
		boolean simpleLengthLessOrEqual = array.length <= 0;
		boolean simpleLengthLessThanOne = array.length < 1;
		boolean simpleLengthNotEqual = array.length != 0;
		boolean simpleLengthGreaterOrEqual = array.length >= 1;
		boolean differentLength = array != null && array.length > 5;
		boolean wrongOperator = array != null || array.length > 0;
		boolean wrongOperatorLessOrEqual = array != null || array.length <= 0;
		boolean wrongOperatorLessThanOne = array != null || array.length < 1;
		boolean wrongOperatorNotEqual = array == null && array.length != 0;
		boolean wrongOperatorGreaterOrEqual = array == null && array.length >= 1;
	}
}
