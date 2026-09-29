#!/usr/bin/env python3
"""Audit tracked justfiles for positional public singular parameters."""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path, PurePosixPath
from typing import Any


def run(command: list[str], *, working_directory: Path) -> subprocess.CompletedProcess[str]:
	return subprocess.run(
		command,
		cwd=working_directory,
		check=False,
		capture_output=True,
		text=True,
	)


def repository_root(path: Path) -> Path:
	result = run(["git", "rev-parse", "--show-toplevel"], working_directory=path)
	if result.returncode != 0:
		raise RuntimeError(result.stderr.strip() or f"{path} is not inside a Git repository")
	return Path(result.stdout.strip()).resolve()


def tracked_justfiles(root: Path) -> list[Path]:
	result = run(["git", "ls-files", "-z"], working_directory=root)
	if result.returncode != 0:
		raise RuntimeError(result.stderr.strip() or f"could not list tracked files in {root}")

	paths = result.stdout.split("\0")
	return sorted(
		root / path
		for path in paths
		if path and is_justfile(PurePosixPath(path).name)
	)


def is_justfile(name: str) -> bool:
	normalized_name = name.casefold()
	return normalized_name in {"justfile", ".justfile"} or normalized_name.startswith("justfile.")


def dump_justfile(justfile: Path) -> dict[str, Any]:
	result = run(
		["just", "--justfile", justfile.name, "--dump", "--dump-format", "json"],
		working_directory=justfile.parent,
	)
	if result.returncode != 0:
		details = result.stderr.strip() or result.stdout.strip() or "just exited without diagnostic output"
		raise RuntimeError(details)

	try:
		data = json.loads(result.stdout)
	except json.JSONDecodeError as error:
		raise RuntimeError(f"just returned invalid JSON: {error}") from error

	if not isinstance(data, dict) or not isinstance(data.get("recipes"), dict):
		raise RuntimeError("just dump JSON does not contain a recipes object")
	return data


def audit_justfile(root: Path, justfile: Path) -> list[str]:
	data = dump_justfile(justfile)
	violations: list[str] = []
	relative_path = justfile.relative_to(root)

	for recipe_name, recipe in sorted(data["recipes"].items()):
		if recipe.get("private"):
			continue

		for parameter in recipe.get("parameters", []):
			if parameter.get("kind") != "singular":
				continue

			parameter_name = parameter.get("name", "<unnamed>")
			location = f"{relative_path}: recipe `{recipe_name}` parameter `{parameter_name}`"
			if parameter.get("long") is None and parameter.get("short") is None:
				violations.append(f"{location} is missing a long or short option")
			elif not str(parameter.get("help") or "").strip():
				violations.append(f"{location} is missing option help text")

	return violations


def parse_arguments() -> argparse.Namespace:
	parser = argparse.ArgumentParser(
		description="Audit every tracked root and nested justfile for public positional singular parameters."
	)
	parser.add_argument(
		"repository",
		nargs="?",
		default=".",
		type=Path,
		help="Git repository to audit (default: current repository)",
	)
	return parser.parse_args()


def main() -> int:
	arguments = parse_arguments()
	try:
		root = repository_root(arguments.repository.resolve())
		justfiles = tracked_justfiles(root)
	except RuntimeError as error:
		print(f"error: {error}", file=sys.stderr)
		return 2

	violations: list[str] = []
	for justfile in justfiles:
		try:
			violations.extend(audit_justfile(root, justfile))
		except RuntimeError as error:
			violations.append(f"{justfile.relative_to(root)}: could not audit: {error}")

	if violations:
		print(f"Found {len(violations)} just option policy violation(s) in {root}:", file=sys.stderr)
		for violation in violations:
			print(f"- {violation}", file=sys.stderr)
		return 1

	print(f"Audited {len(justfiles)} tracked justfile(s) in {root}: no option policy violations.")
	return 0


if __name__ == "__main__":
	raise SystemExit(main())
