import io.dropwizard.jersey.params.IntParam;

class Test {
	void intParamStaysInParams(IntParam intParam) {
		int value = intParam.get();
	}

	void alreadyUsingRawTypes(Boolean flag, java.time.Duration duration) {
		boolean b = flag;
		long millis = duration.toMillis();
	}
}
