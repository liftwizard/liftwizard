import ch.qos.logback.access.spi.IAccessEvent;

class FakeAccessEvent
	implements IAccessEvent
{
	@Override
	public String getRequestURL()
	{
		return "https://example.com";
	}
}
