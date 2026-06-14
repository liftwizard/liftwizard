// Formatting config for `vp fmt` (oxfmt). Plain object export so no
// node_modules are needed; vite-plus is provided globally via mise.
export default {
	fmt: {
		useTabs: true,
		tabWidth: 4,
		printWidth: 120,
		semi: true,
		singleQuote: true,
		bracketSpacing: false,
		trailingComma: 'all',
		arrowParens: 'always',
	},
};
