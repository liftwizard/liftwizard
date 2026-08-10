import org.apache.log4j.Level;
import org.apache.log4j.Logger;

class CustomLogger {
	void log(String message) {}
}

class Test {
	private static final Logger LOGGER = Logger.getLogger(Test.class);
	private final CustomLogger custom = new CustomLogger();

	void doesNotDetectLevelMethods(String message) {
		LOGGER.debug(message);
		LOGGER.info(message);
		LOGGER.warn(message);
		LOGGER.error(message);
	}

	void doesNotDetectBareLevelConstant() {
		Level level = Level.INFO;
	}

	void doesNotDetectUnrelatedLog(String message) {
		custom.log(message);
	}
}
