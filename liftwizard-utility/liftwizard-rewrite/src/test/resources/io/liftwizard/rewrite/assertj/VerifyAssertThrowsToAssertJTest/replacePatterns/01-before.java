import java.util.concurrent.Callable;
import org.eclipse.collections.impl.test.Verify;

class Test
{
	void test()
	{
		Verify.assertThrows(IllegalArgumentException.class, () ->
		{
			throw new IllegalArgumentException("error");
		});

		Verify.assertThrows(NullPointerException.class, () ->
		{
			throw new NullPointerException();
		});

		Callable<Object> failingCallable = () ->
		{
			throw new RuntimeException("error");
		};
		Verify.assertThrows(RuntimeException.class, failingCallable);
	}
}
