import assert from "node:assert/strict";
import {readdirSync, readFileSync} from "node:fs";
import {test} from "node:test";
import {isDeepStrictEqual} from "node:util";

// Pull request and merge queue runs can restore caches saved on main, but not each other's. Caches saved outside main only help reruns of the same pull request, crowd main's caches out of the repository's cache quota, and let one job's restore-keys fallback pick up another job's cache that lacks its plugins. So main saves every Maven cache and the other workflows only restore main's keys.
// The keys hash push.yml so that changing a warm-up step saves a fresh cache instead of hitting one saved without it. The -main- prefixes keep the restore-keys fallback away from the per-job caches that older workflows saved on pull request branches, which a pull request run would otherwise restore before main's cache.

const workflowsDirectory = new URL("./workflows/", import.meta.url);
const hashedFiles = "hashFiles('**/pom.xml', '.github/workflows/push.yml')";
const mavenCacheKeys = [`maven-main-\${{ ${hashedFiles} }}`, `codegenome-maven-rewrite-main-\${{ ${hashedFiles} }}`];
// The codegenome caches may fall back to main's plain Maven cache, but never the other way around, since a plain Maven build rechecks artifacts cached as coming from codegenome against Central.
const restoreKeysByCacheKey = new Map([
	[mavenCacheKeys[0], ["maven-main-"]],
	[mavenCacheKeys[1], ["codegenome-maven-rewrite-main-", "maven-main-"]],
]);

function workflowLines(workflow)
{
	return readFileSync(new URL(workflow, workflowsDirectory), "utf8").split("\n");
}

// Maps each job name to the text of that job's lines.
function jobs(workflow)
{
	const jobTexts = new Map();
	let job;
	for (const line of workflowLines(workflow))
	{
		const jobStart = /^ {2}([A-Za-z0-9_-]+):\s*$/.exec(line);
		if (jobStart)
		{
			job = jobStart[1];
			jobTexts.set(job, "");
		}
		else if (job !== undefined && /^\S/.test(line))
		{
			job = undefined;
		}
		if (job !== undefined)
		{
			jobTexts.set(job, `${jobTexts.get(job)}${line}\n`);
		}
	}
	return jobTexts;
}

function cacheSteps(workflow)
{
	const lines = workflowLines(workflow);
	const steps = [];
	let job;
	for (let index = 0; index < lines.length; index++)
	{
		const jobStart = /^ {2}([A-Za-z0-9_-]+):\s*$/.exec(lines[index]);
		if (jobStart)
		{
			job = jobStart[1];
		}
		const start = /^(\s*)- uses: (actions\/cache[^@]*)@/.exec(lines[index]);
		if (!start)
		{
			continue;
		}
		const body = [];
		while (
			index + 1 < lines.length &&
			(lines[index + 1].trim() === "" || lines[index + 1].search(/\S/) > start[1].length)
		)
		{
			body.push(lines[++index].trim());
		}
		const value = (name) =>
			body
				.find((line) => line.startsWith(`${name}:`))
				?.slice(name.length + 1)
				.trim();
		const restoreKeysStart = body.indexOf("restore-keys: |");
		const restoreKeys = [];
		for (
			let line = restoreKeysStart + 1;
			restoreKeysStart >= 0 && line < body.length && !/^[a-z-]+:/.test(body[line]);
			line++
		)
		{
			if (body[line] !== "")
			{
				restoreKeys.push(body[line]);
			}
		}
		steps.push(
			{
				workflow,
				job,
				action: start[2],
				id: value("id"),
				key: value("key"),
				path: value("path"),
				restoreKeys,
			},
		);
	}
	return steps;
}

const mavenSteps = readdirSync(workflowsDirectory)
	.filter((file) => file.endsWith(".yml"))
	.flatMap(cacheSteps)
	.filter((step) => step.path === "~/.m2/repository");
const savingSteps = mavenSteps.filter((step) => step.action !== "actions/cache/restore");

test("only push.yml saves Maven caches, and it saves every key the other workflows restore", () =>
{
	assert.deepEqual(
		savingSteps.map(({workflow, key}) => (
		{
			workflow,
			key,
		})),
		mavenCacheKeys.map((key) => (
		{
			workflow: "push.yml",
			key,
		})),
	);
});

test("every Maven cache restores only main's keys, and only the codegenome caches fall back to the other key family", () =>
{
	assert.deepEqual(
		mavenSteps.filter((step) => !isDeepStrictEqual(step.restoreKeys, restoreKeysByCacheKey.get(step.key))),
		[],
	);
});

test("each push.yml job warms up only on its own cache miss, since a hit was saved by the same warm-up from the same POMs", () =>
{
	assert.deepEqual(
		savingSteps.filter(
			(step) =>
				step.id === undefined ||
				!jobs(step.workflow).get(step.job).includes(`if: steps.${step.id}.outputs.cache-hit != 'true'`),
		),
		[],
	);
});
