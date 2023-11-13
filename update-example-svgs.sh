#!/usr/bin/env bash

if ! command -v asciinema &> /dev/null; then
    echo "Command 'asciinema' not found. Please install and put on path."
    exit
fi
if ! command -v agg &> /dev/null; then
    echo "Command 'agg' not found. Please install and put on path."
    exit
fi

set -e

REPO_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Ensure sample binaries are available
"$REPO_DIR/gradlew" -q --console plain -p "$REPO_DIR" installDist

for sample in $REPO_DIR/samples/*; do
	sample_name=$(basename "$sample")
	sample_bin="$sample/build/install/$sample_name/bin/$sample_name"

	if test -f "$sample_bin"; then
		echo "Capturing $sample_name..."

		command="'$sample_bin' 2>/dev/null && sleep 2 && printf ' \e[D'"
		if [ -f "$sample/input.sh" ]; then
			command="'$sample/input.sh' | $command"
		fi

		echo "Running $command..."
		rm -f $sample/demo.cast
		asciinema rec -c "$command" $sample/demo.cast
		agg --cols 60 --rows 18 $sample/demo.cast $sample/demo.gif
		rm $sample/demo.cast
	fi
done

echo "Done"
