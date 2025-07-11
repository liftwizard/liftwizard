`io.liftwizard.tempdir.ManagedTempDirectory` creates temporary directories that are deleted when you close them, with a JVM shutdown hook as a fallback for directories that were never closed.

## Problem

When creating temporary directories in Java, it's easy to forget to clean them up, leading to disk space issues.

## Solution

`ManagedTempDirectory` implements `AutoCloseable`, so a try-with-resources block deletes the directory and its contents. Every directory is also registered with a shutdown hook that deletes whatever is still open when the JVM exits.

## Usage

### Try-with-resources

```java
try (ManagedTempDirectory tempDir = ManagedTempDirectory.create("test-")) {
    Path path = tempDir.getPath();
    Files.writeString(path.resolve("data.txt"), "content");
}
// The directory and its contents are deleted when the block exits
```

### Path Deleted Only at Shutdown

`createTempDirectory` returns a plain `Path` and keeps no handle you can close, so the directory is only deleted by the shutdown hook:

```java
Path tempDir = ManagedTempDirectory.createTempDirectory("test-");
```

Directories created this way stay on disk until the JVM exits. In a long-running service, don't call it once per request or job; use `create` with try-with-resources instead.

### Handling Deletion Failures

`close()` throws a `RuntimeException` when deletion fails, for example when a file is still open on Windows. `tryClose()` logs the failure and returns `false` instead:

```java
ManagedTempDirectory tempDir = ManagedTempDirectory.create("test-");
// Use the directory...
boolean deleted = tempDir.tryClose();
```

After a failure, deletion deletes everything it can and the directory stays registered. Calling `close()` or `tryClose()` again retries, and the shutdown hook retries at exit. Once the directory is deleted, later calls do nothing, and `tryClose()` returns `true`.

## Features

### Shutdown Hook

The shutdown hook calls `tryClose()` on every directory that is still open, so one directory that can't be deleted doesn't stop the others from being cleaned up. It runs when:

- The application exits normally
- `System.exit()` is called
- The JVM receives termination signals like SIGTERM

Shutdown hooks do not run if the JVM is killed forcefully (e.g., `kill -9`) or crashes.

The JVM runs shutdown hooks concurrently and in no set order, so this hook can run while Dropwizard's own hook is still stopping managed objects. Close directories that are used during shutdown yourself rather than relying on the hook. The logging framework's shutdown hook may also stop appenders first, so messages from this hook can be lost.

The hook is registered when the class is loaded, once per classloader.

### Thread Safety

Directories can be created from any thread. `close()` and `tryClose()` are synchronized on the instance, so a second caller waits for the first caller's deletion to finish.

### Logging

Creation and deletion are logged at DEBUG level, and deletion failures at WARN level, using SLF4J.

### File Attributes

You can pass file attributes when creating the directory. On POSIX systems, the JDK already creates temporary directories readable only by their owner. This example also grants the owner's group read access:

```java
ManagedTempDirectory tempDir = ManagedTempDirectory.create(
    "shared-",
    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---")));
```

POSIX attributes are not supported on Windows, where `create` throws `UnsupportedOperationException`.

## Recursive Deletion

`io.liftwizard.tempdir.RecursiveDirectoryDeleter` deletes a directory tree. When a file or directory can't be deleted, it keeps going and deletes everything else it can. `deleteRecursively` then throws the first `IOException`, with later failures attached as suppressed exceptions, and `tryDeleteRecursively` returns `false`.

## Maven Dependency

The version is managed by `liftwizard-bom`.

```xml
<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-temp-files</artifactId>
</dependency>
```
