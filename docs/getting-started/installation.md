---
title: Installation
parent: Getting Started
nav_order: 1
---

# Installation

## Prerequisites

Before installing Spring-Hex CLI, ensure you have:

- **Java 17 or higher**: Verify with `java -version`
- **A Spring Boot project**: Either existing or newly created

## Quick Install (Recommended)

Run the install script — it downloads the JAR, creates a wrapper, and adds it to your PATH:

```bash
curl -fsSL https://raw.githubusercontent.com/Spring-hex/Spring-hex.github.io/master/install.sh | bash
```

Then reload your shell:

```bash
source ~/.zshrc  # or ~/.bashrc
```

That's it. Verify with:

```bash
spring-hex --version
```

---

## Manual Install

If you prefer to install manually:

### 1. Download the JAR

```bash
curl -fsSL -o spring-hex-cli.jar \
  https://github.com/Spring-hex/Spring-hex.github.io/releases/latest/download/spring-hex-cli-1.0.0.jar
```

### 2. Choose a setup method

**Option A: Shell Alias**

Add to your `~/.bashrc` or `~/.zshrc`:

```bash
alias spring-hex='java -jar /path/to/spring-hex-cli.jar'
```

**Option B: Wrapper Script**

```bash
mkdir -p ~/.spring-hex
mv spring-hex-cli.jar ~/.spring-hex/

cat > ~/.spring-hex/spring-hex << 'EOF'
#!/bin/bash
exec java -jar "$HOME/.spring-hex/spring-hex-cli.jar" "$@"
EOF
chmod +x ~/.spring-hex/spring-hex

# Add to PATH (add this line to your ~/.bashrc or ~/.zshrc)
export PATH="$HOME/.spring-hex:$PATH"
```

---

## Build from Source

```bash
git clone https://github.com/Spring-hex/Spring-hex.github.io.git
cd Spring-hex.github.io
mvn clean package
java -jar target/spring-hex-cli-1.0.0.jar --version
```

## Verification

```bash
spring-hex --version
```

Expected output:

```
Spring-Hex CLI v1.0.0
```

## Uninstall

```bash
rm -rf ~/.spring-hex
# Then remove the PATH line from your ~/.bashrc or ~/.zshrc
```

## Next Steps

Continue to [Initialization]({% link getting-started/initialization.md %}) to configure Spring-Hex CLI for your project.
