#!/usr/bin/env bash

if ! command -v svg-term &> /dev/null; then
    echo "Command 'svg-term' not found. Please install with 'npm install -g svg-term-cli'."
    exit
fi

set -e

REPO_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Ensure sample binaries are available
"$REPO_DIR/gradlew" -q --console plain -p "$REPO_DIR/samples" installDist

for sample in $REPO_DIR/samples/*; do
	sample_name=$(basename "$sample")
	sample_bin="$sample/build/install/$sample_name/bin/$sample_name"

	if test -f "$sample_bin"; then
		echo "Capturing $sample_name..."

		command="'$sample_bin' 2>/dev/null && sleep 2 && echo"
		if [ -f "$sample/input.sh" ]; then
			command="'$sample/input.sh' | $command"
		fi

		echo "Running $command..."
		svg-term "--command=$command" "--out=$sample/demo.svg" --from=50 --window --width=60 --height=16 --no-cursor
		cat > "$sample/README.md" <<EOL
# Example: $sample_name

<img src="demo.svg">
EOL
	fi
done

echo "Done"
