export default {
	fmt: {
		useTabs: true,
		tabWidth: 4,
		printWidth: 120,
		semi: true,
		singleQuote: false,
		bracketSpacing: false,
		trailingComma: "all",
		arrowParens: "always",
		overrides: [
			{
				files: [".yamllint.yaml", "**/*.yaml", "**/*.yml"],
				options: {
					tabWidth: 2,
					useTabs: false,
				},
			},
		],
	},
};
