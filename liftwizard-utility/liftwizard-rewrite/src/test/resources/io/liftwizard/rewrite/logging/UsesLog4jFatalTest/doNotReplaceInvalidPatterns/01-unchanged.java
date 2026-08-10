import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Optional;

class CustomLogger {
	void fatal(String message) {}
}

class CustomLevel {
	public static final String FATAL = "FATAL";
}

class Test {
	private static final Logger LOGGER = Logger.getLogger(Test.class);
	private final CustomLogger custom = new CustomLogger();

	void doesNotDetectOtherLevels(String message) {
		LOGGER.debug(message);
		LOGGER.info(message);
		LOGGER.warn(message);
		LOGGER.error(message);
	}

	void doesNotDetectOtherLevelConstants() {
		Level error = Level.ERROR;
		Level debug = Level.DEBUG;
		LOGGER.log(Level.ERROR, "oops");
	}

	void doesNotDetectOtherLevelMethodReference(Optional<Object> value) {
		value.ifPresent(LOGGER::error);
	}

	void doesNotDetectUnrelatedFatal(String message) {
		custom.fatal(message);
	}

	void doesNotDetectUnrelatedFatalConstant() {
		String fatal = CustomLevel.FATAL;
	}
}
