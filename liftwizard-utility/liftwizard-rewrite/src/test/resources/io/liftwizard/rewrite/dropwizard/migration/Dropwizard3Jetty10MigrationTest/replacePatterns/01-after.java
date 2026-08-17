import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;

class MyComponent
{
	void onAbstractLifeCycle(AbstractLifeCycle lifecycle, LifeCycle.Listener listener)
	{
		lifecycle.addEventListener(listener);
	}

	void onContainerLifeCycle(ContainerLifeCycle lifecycle, LifeCycle.Listener listener)
	{
		lifecycle.addEventListener(listener);
	}
}
