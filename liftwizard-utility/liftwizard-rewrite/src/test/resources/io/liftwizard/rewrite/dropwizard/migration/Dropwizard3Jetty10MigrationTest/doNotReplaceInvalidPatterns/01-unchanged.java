import java.util.EventListener;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

class MyComponent
{
	void alreadyUsingAddEventListener(AbstractLifeCycle lifecycle, EventListener listener)
	{
		lifecycle.addEventListener(listener);
	}
}
