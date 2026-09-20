#!/usr/bin/env bash

# Yes, this is AI-generated

set -euo pipefail

# Locate directories relative to this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PARENT_DIR="$(cd "$REPO_ROOT/.." && pwd)"

ACCESSKIT_DIR="$PARENT_DIR/accesskit"
ACCESSKIT_C_DIR="$PARENT_DIR/accesskit-c"
DEST_DIR="$REPO_ROOT/lib/src/main/resources/natives"

echo "=== Ensuring Rust is available ==="
if ! command -v cargo &> /dev/null; then
    echo "Rust/Cargo not found. Installing via rustup..."
    curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y --default-toolchain stable
    source "$HOME/.cargo/env"
else
    # Ensure PATH contains cargo if already installed in home directory
    [ -f "$HOME/.cargo/env" ] && source "$HOME/.cargo/env"
fi

echo "=== Preparing sibling repositories ==="
# 1. Clone BAB fork into ../accesskit if missing
if [ ! -d "$ACCESSKIT_DIR" ]; then
    echo "Cloning accesskit-bab-fork into $ACCESSKIT_DIR..."
    git clone --depth 1 https://github.com/BuildABrowser/accesskit-bab-fork "$ACCESSKIT_DIR"
else
    echo "Found existing $ACCESSKIT_DIR"
fi

# 2. Clone accesskit-c into ../accesskit-c if missing
if [ ! -d "$ACCESSKIT_C_DIR" ]; then
    echo "Cloning accesskit-c into $ACCESSKIT_C_DIR..."
    git clone --depth 1 https://github.com/AccessKit/accesskit-c "$ACCESSKIT_C_DIR"
else
    echo "Found existing $ACCESSKIT_C_DIR"
fi

# 3. Overwrite Cargo.toml with the bundled version
echo "Applying bundled Cargo.toml..."
cp "$SCRIPT_DIR/accesskit-c.Cargo.toml" "$ACCESSKIT_C_DIR/Cargo.toml"

# 4. Build release binary
echo "Compiling native library..."
(
    cd "$ACCESSKIT_C_DIR"
    cargo build --release
)

# 5. Copy and strip symbols
mkdir -p "$DEST_DIR"
SRC_SO="$ACCESSKIT_C_DIR/target/release/libaccesskit.so"
DEST_SO="$DEST_DIR/libaccesskit_x86_64.so"

echo "Stripping symbols and installing to $DEST_SO..."
# --strip-unneeded preserves dynamic symbols required by dlopen/FFM
strip --strip-unneeded -o "$DEST_SO" "$SRC_SO"

echo "=== Native build complete ==="
ls -lh "$DEST_SO"