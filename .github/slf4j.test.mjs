import assert from "node:assert/strict";
import {readFileSync} from "node:fs";
import {test} from "node:test";

const config = JSON.parse(readFileSync(new URL("./slf4j.json", import.meta.url), "utf8"));

function findMatch(line) {
	for (const matcher of config.problemMatcher) {
		const [pattern] = matcher.pattern;
		const match = new RegExp(pattern.regexp).exec(line);
		if (match) {
			return {owner: matcher.owner, severity: matcher.severity, message: match[pattern.message]};
		}
	}
	return undefined;
}

const errorLines = [
	// slf4j-api 1.7 with no binding on the classpath
	'SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".',
	'SLF4J: Failed to load class "org.slf4j.impl.StaticMDCBinder".',
	// slf4j-api 1.7 with more than one binding
	"SLF4J: Class path contains multiple SLF4J bindings.",
	// slf4j-api 1.7 with a binding for another major version
	"SLF4J: The requested version 1.5.6 by your slf4j binding is not compatible with [1.6, 1.7]",
	"SLF4J: slf4j-api 1.6.x (or later) is incompatible with this binding.",
	"SLF4J: Failed to instantiate SLF4J LoggerFactory",
	// Bridge and binding for the same framework, which would loop forever
	"SLF4J: Detected both log4j-over-slf4j.jar AND bound slf4j-log4j12.jar on the class path, preempting StackOverflowError. ",
	// slf4j-api 2.x with no provider on the classpath
	"SLF4J: No SLF4J providers were found.",
	"SLF4J(W): No SLF4J providers were found.",
	// slf4j-api 2.x with more than one provider
	"SLF4J: Class path contains multiple SLF4J providers.",
	"SLF4J(W): Class path contains multiple SLF4J providers.",
	// slf4j-api 2.x with only 1.7-era bindings, which it ignores
	"SLF4J: Class path contains SLF4J bindings targeting slf4j-api versions prior to 1.8.",
	"SLF4J(W): Class path contains SLF4J bindings targeting slf4j-api versions 1.7.x or earlier.",
	"SLF4J(E): A service provider failed to instantiate:",
	"SLF4J(E): Failed to instantiate the specified SLF4JServiceProvider (com.example.MissingProvider)",
];

const warningLines = [
	"SLF4J: Failed toString() invocation on an object of type [com.example.Widget]",
	"SLF4J(E): Failed toString() invocation on an object of type [com.example.Widget]",
];

// Informational lines that accompany an error already reported, and replay notices that are expected while SLF4J bootstraps.
const ignoredLines = [
	"SLF4J: Defaulting to no-operation (NOP) logger implementation",
	"SLF4J(W): Defaulting to no-operation (NOP) logger implementation",
	"SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.",
	"SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.",
	"SLF4J: Found binding in [jar:file:/home/runner/.m2/repository/ch/qos/logback/logback-classic/1.2.13/logback-classic-1.2.13.jar!/org/slf4j/impl/StaticLoggerBinder.class]",
	"SLF4J: Actual binding is of type [ch.qos.logback.classic.util.ContextSelectorStaticBinder]",
	"SLF4J(I): Connected with provider of type [ch.qos.logback.classic.spi.LogbackServiceProvider]",
	"SLF4J: A number (3) of logging calls during the initialization phase have been intercepted and are",
	"SLF4J: now being replayed. These are subject to the filtering rules of the underlying logging system.",
	"SLF4J: See also http://www.slf4j.org/codes.html#replay",
	"SLF4J: The following set of substitute loggers may have been accessed",
	"SLF4J: during the initialization phase. Logging calls during this",
	"[INFO] Liftwizard JUnit Extension: Log SLF4J Markers ...... SUCCESS [  1.449 s]",
	"[INFO] Building Liftwizard JUnit Rule: Log SLF4J Markers 2.1.50-SNAPSHOT [79/169]",
];

for (const line of errorLines) {
	test(`error: ${line}`, () => {
		const match = findMatch(line);
		assert.equal(match?.severity, "error");
		assert.equal(match.message, line.replace(/^SLF4J(\([EWI]\))?: /, "").trim());
	});
}

for (const line of warningLines) {
	test(`warning: ${line}`, () => {
		const match = findMatch(line);
		assert.equal(match?.severity, "warning");
		assert.equal(match.message, line.replace(/^SLF4J(\([EWI]\))?: /, "").trim());
	});
}

for (const line of ignoredLines) {
	test(`ignored: ${line}`, () => {
		assert.equal(findMatch(line), undefined);
	});
}
