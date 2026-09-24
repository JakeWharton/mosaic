#!/bin/bash

set -e

JVM_ONLY_SAMPLES=("java-tty")

usage() {
  echo "Usage: $0 <sample_name> [command] [target]"
  echo ""
  echo "Builds and/or runs a sample."
  echo ""
  echo "Commands:"
  echo "  build       Build the sample"
  echo "  run         Run the sample"
  echo "  buildAndRun Build, then run the sample (default)"
  echo ""
  echo "Targets:"
  echo "  jvm         Build/Run for JVM (default)"
  echo "  native      Build/Run for native"
  echo ""
  echo "Examples:"
  echo "  $0 counter              # Build and run for JVM"
  echo "  $0 counter native       # Build and run for native"
  echo "  $0 counter build        # Just build for JVM"
  echo "  $0 counter run native   # Just run the native build"
  exit 1
}

build_sample() {
    if [ "$TARGET" == "jvm" ]; then
      if [ "$is_jvm_only" -eq 1 ]; then
        ./gradlew ":samples:$SAMPLE_NAME:installDist"
      else
        ./gradlew ":samples:$SAMPLE_NAME:installJvmDist"
      fi
    elif [ "$TARGET" == "native" ]; then
      ./gradlew ":samples:$SAMPLE_NAME:$NATIVE_BUILD_TASK"
    fi
}

run_sample() {
    if [ "$TARGET" == "jvm" ]; then
      if [ "$is_jvm_only" -eq 1 ]; then
        "./samples/$SAMPLE_NAME/build/install/$SAMPLE_NAME/bin/$SAMPLE_NAME"
      else
        "./samples/$SAMPLE_NAME/build/install/$SAMPLE_NAME-jvm/bin/$SAMPLE_NAME"
      fi
    elif [ "$TARGET" == "native" ]; then
      "$EXE_PATH"
    fi
}

if [ "$#" -lt 1 ]; then
  usage
fi

SAMPLE_NAME=$1
COMMAND=""
TARGET=""

if [[ "$2" == "build" || "$2" == "run" || "$2" == "buildAndRun" ]]; then
  COMMAND=$2
  TARGET=${3:-jvm}
elif [[ "$2" == "jvm" || "$2" == "native" ]]; then
  COMMAND="buildAndRun"
  TARGET=$2
elif [ -z "$2" ]; then
  COMMAND="buildAndRun"
  TARGET="jvm"
else
  echo "Invalid command or target: $2"
  usage
fi

is_jvm_only=0
for s in "${JVM_ONLY_SAMPLES[@]}"; do
  if [ "$s" == "$SAMPLE_NAME" ]; then
    is_jvm_only=1
    break
  fi
done

if [ "$is_jvm_only" -eq 1 ] && [ "$TARGET" == "native" ]; then
  echo "Error: The '$SAMPLE_NAME' sample is JVM-only and does not support the 'native' target." >&2
  exit 1
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR/.."

if [ "$TARGET" == "native" ]; then
  UNAME_S=$(uname -s)
  UNAME_M=$(uname -m)
  case "${UNAME_S}" in
    Linux*)
      case "${UNAME_M}" in
        x86_64)  TARGET_NAME="linuxX64" ;;
        aarch64) TARGET_NAME="linuxArm64" ;;
        *) >&2 echo "Unsupported Linux architecture: ${UNAME_M}"; exit 1 ;;
      esac
      ;;
    Darwin*)
      case "${UNAME_M}" in
        x86_64)  TARGET_NAME="macosX64" ;;
        arm64)   TARGET_NAME="macosArm64" ;;
        *) >&2 echo "Unsupported macOS architecture: ${UNAME_M}"; exit 1 ;;
      esac
      ;;
    CYGWIN*|MINGW*|MSYS*)
      case "${UNAME_M}" in
        x86_64)  TARGET_NAME="mingwX64" ;;
        *) >&2 echo "Unsupported Windows architecture: ${UNAME_M}"; exit 1 ;;
      esac
      ;;
    *) >&2 echo "Unsupported OS: ${UNAME_S}"; exit 1 ;;
  esac

  FIRST_CHAR=$(printf "%.1s" "$TARGET_NAME" | tr '[:lower:]' '[:upper:]')
  REMAINING_CHARS="${TARGET_NAME:1}"
  C_TARGET_NAME="${FIRST_CHAR}${REMAINING_CHARS}"
  NATIVE_BUILD_TASK="linkReleaseExecutable${C_TARGET_NAME}"
  
  EXE_PATH="./samples/$SAMPLE_NAME/build/bin/${TARGET_NAME}/releaseExecutable/$SAMPLE_NAME"
  if [[ "$TARGET_NAME" == "mingw"* ]]; then
    EXE_PATH="${EXE_PATH}.exe"
  else
    EXE_PATH="${EXE_PATH}.kexe"
  fi
fi

case "$COMMAND" in
  build)
    build_sample
    ;;
  run)
    run_sample
    ;;
  buildAndRun)
    if ! OUTPUT=$(build_sample 2>&1); then
      echo "$OUTPUT"
      exit 1
    fi
    run_sample
    ;;
  *)
    echo "Internal error: Unknown command '$COMMAND'"
    usage
    ;;
esac
