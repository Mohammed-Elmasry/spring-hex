#!/bin/bash
set -e

VERSION="${1:-latest}"
INSTALL_DIR="$HOME/.spring-hex"
JAR_NAME="spring-hex-cli.jar"
REPO="Spring-hex/Spring-hex.github.io"

echo "Installing Spring Hex CLI..."

# Determine download URL
if [ "$VERSION" = "latest" ]; then
    DOWNLOAD_URL="https://github.com/$REPO/releases/latest/download/spring-hex-cli-1.0.0.jar"
else
    DOWNLOAD_URL="https://github.com/$REPO/releases/download/$VERSION/spring-hex-cli-1.0.0.jar"
fi

# Check for Java
if ! command -v java &> /dev/null; then
    echo "Error: Java 17+ is required but not found."
    echo "Install Java and try again."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ] 2>/dev/null; then
    echo "Error: Java 17+ is required. Found Java $JAVA_VERSION."
    exit 1
fi

# Create install directory
mkdir -p "$INSTALL_DIR"

# Download JAR
echo "Downloading from $DOWNLOAD_URL..."
if command -v curl &> /dev/null; then
    curl -fsSL -o "$INSTALL_DIR/$JAR_NAME" "$DOWNLOAD_URL"
elif command -v wget &> /dev/null; then
    wget -q -O "$INSTALL_DIR/$JAR_NAME" "$DOWNLOAD_URL"
else
    echo "Error: curl or wget is required."
    exit 1
fi

# Create wrapper script
cat > "$INSTALL_DIR/spring-hex" << 'WRAPPER'
#!/bin/bash
exec java -jar "$HOME/.spring-hex/spring-hex-cli.jar" "$@"
WRAPPER
chmod +x "$INSTALL_DIR/spring-hex"

# Add to PATH if not already there
SHELL_RC=""
if [ -f "$HOME/.zshrc" ]; then
    SHELL_RC="$HOME/.zshrc"
elif [ -f "$HOME/.bashrc" ]; then
    SHELL_RC="$HOME/.bashrc"
fi

if [ -n "$SHELL_RC" ]; then
    if ! grep -q '.spring-hex' "$SHELL_RC" 2>/dev/null; then
        echo '' >> "$SHELL_RC"
        echo '# Spring Hex CLI' >> "$SHELL_RC"
        echo 'export PATH="$HOME/.spring-hex:$PATH"' >> "$SHELL_RC"
        echo "Added to PATH in $SHELL_RC"
    fi
fi

echo ""
echo "Spring Hex CLI installed successfully!"
echo ""
echo "To use it now, run:"
echo "  export PATH=\"\$HOME/.spring-hex:\$PATH\""
echo ""
echo "Then verify with:"
echo "  spring-hex --version"
echo ""
