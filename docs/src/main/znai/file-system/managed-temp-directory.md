# ManagedTempDirectory

`ManagedTempDirectory` provides automatic cleanup of temporary directories using JVM shutdown hooks.

## Problem

When creating temporary directories in Java, it's easy to forget to clean them up, leading to disk space issues.

## Solution

`ManagedTempDirectory` automatically registers a shutdown hook to clean up temporary directories when the JVM exits. It implements `AutoCloseable` for use with try-with-resources blocks, providing both explicit cleanup and automatic cleanup as a safety net.

## Usage

### Basic Usage with AutoCloseable

```java
try (ManagedTempDirectory tempDir = ManagedTempDirectory.create("test-")) {
    Path path = tempDir.getPath();
    // Use the temporary directory
    Files.write(path.resolve("data.txt"), "content".getBytes());
}
// Directory is automatically deleted when exiting the try block
```

### Static Factory Method

For cases where you don't need the `ManagedTempDirectory` instance:

```java
Path tempDir = ManagedTempDirectory.createTempDirectory("test-");
// Directory will be cleaned up on JVM shutdown
```

### Manual Cleanup

You can explicitly close the directory when done:

```java
ManagedTempDirectory tempDir = ManagedTempDirectory.create("test-");
Path path = tempDir.getPath();
// Use the directory...
tempDir.close(); // Explicitly delete the directory
```

### Safe Cleanup with tryClose()

For cases where deletion might fail (e.g., files still in use):

```java
ManagedTempDirectory tempDir = ManagedTempDirectory.create("test-");
// Use the directory...
boolean deleted = tempDir.tryClose(); // Returns true if successfully deleted
```

## Features

### Automatic Shutdown Hook

All created directories are registered with a JVM shutdown hook, ensuring cleanup when:

- The application exits normally
- `System.exit()` is called
- The JVM receives termination signals like SIGTERM

Shutdown hooks do not run if the JVM is killed forcefully (e.g., `kill -9`) or crashes.

### Thread-Safe

The implementation uses thread-safe collections and atomic operations, making it safe to use from multiple threads.

### Logging

The class logs directory creation and deletion at DEBUG level using SLF4J, helping with debugging and monitoring.

### File Attributes Support

You can specify file attributes when creating directories:

```java
ManagedTempDirectory tempDir = ManagedTempDirectory.create("secure-",
    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
```

## Implementation Details

The class uses:

- `ConcurrentHashMap` to track active directories
- `AtomicBoolean` to ensure directories are only deleted once
- A static shutdown hook registered once per JVM
- `RecursiveDirectoryDeleter` for reliable recursive deletion

## Maven Dependency

```xml
<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-temp-files</artifactId>
</dependency>
```
