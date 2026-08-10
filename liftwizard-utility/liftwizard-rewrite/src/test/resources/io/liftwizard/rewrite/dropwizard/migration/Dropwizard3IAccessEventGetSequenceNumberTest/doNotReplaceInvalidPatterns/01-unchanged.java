import ch.qos.logback.access.spi.IAccessEvent;

// Already declares getSequenceNumber — leave alone.
class AlreadyImplemented implements IAccessEvent {

	@Override
	public String getRequestURL() {
		return "url";
	}

	@Override
	public long getSequenceNumber() {
		return 42L;
	}
}

// Not an IAccessEvent — even if name collides, leave alone.
class Unrelated {

	public long getRequestURL() {
		return 0;
	}
}

// Interface that re-declares the method but isn't a concrete impl — skip.
interface SubAccessEvent extends IAccessEvent {
	String extraMethod();
}
