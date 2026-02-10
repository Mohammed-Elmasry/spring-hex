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

## Download

Download the latest JAR file from the [GitHub Releases](https://github.com/spring-hex/spring-hex/releases) page:

```bash
# Example download URL (replace with actual release)
curl -L -o spring-hex-cli-1.0.0.jar \
  https://github.com/spring-hex/spring-hex/releases/download/v1.0.0/spring-hex-cli-1.0.0.jar
```

## Setup

### Option 1: Shell Alias (Recommended)

Create a shell alias for convenient access. Add this line to your shell configuration file (`~/.bashrc`, `~/.zshrc`, or equivalent):

```bash
alias spring-hex='java -jar /path/to/spring-hex-cli-1.0.0.jar'
```

Replace `/path/to/` with the actual location where you saved the JAR file.

Reload your shell configuration:

```bash
source ~/.bashrc  # or ~/.zshrc
```

### Option 2: Add to PATH

For system-wide access, move the JAR to a directory in your PATH and create a wrapper script:

```bash
# Move JAR to a bin directory
sudo mkdir -p /usr/local/bin
sudo mv spring-hex-cli-1.0.0.jar /usr/local/bin/

# Create wrapper script
sudo tee /usr/local/bin/spring-hex > /dev/null << 'EOF'
#!/bin/bash
exec java -jar /usr/local/bin/spring-hex-cli-1.0.0.jar "$@"
EOF

# Make executable
sudo chmod +x /usr/local/bin/spring-hex
```

## Verification

Verify the installation by checking the version:

```bash
spring-hex --version
```

Expected output:

```
Spring-Hex CLI v1.0.0
```

If you see the version number, the installation is successful.

## Next Steps

Continue to [Initialization]({% link getting-started/initialization.md %}) to configure Spring-Hex CLI for your project.
