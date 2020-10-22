#!/usr/bin/env bash

if ! command -v asciinema &> /dev/null; then
    echo "Command 'asciinema' not found. Please install and add to your PATH."
    exit
fi
if ! command -v asciinema-vsync &> /dev/null; then
    echo "Command 'asciinema-vsync' not found. Please install and add to your PATH."
    exit
fi
if ! command -v svg-term &> /dev/null; then
    echo "Command 'svg-term' not found. Please install with 'npm install -g svg-term-cli'."
    exit
fi

set -e

REPO_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Ensure example binaries are available
echo "Building..."
"$REPO_DIR/gradlew" -q --console plain -p "$REPO_DIR" installDist

for example in $REPO_DIR/examples/*/; do
	example_name=$(basename "$example")
	echo "Capturing $example_name..."
	asciinema rec -c "'$example/build/install/$example_name/bin/$example_name' && sleep 2 && echo" "$example/demo_raw.json"
	asciinema-vsync "$example/demo_raw.json" "$example/demo.json"
	svg-term --in "$example/demo.json" --out="$example/demo.svg" --from=50 --window --width=50 --height=16 --no-cursor
	rm "$example/demo_raw.json" "$example/demo.json"
	cat > "$example/README.md" <<EOL
# Example: $example_name

<img src="demo.svg">
EOL
done

echo "Done"
