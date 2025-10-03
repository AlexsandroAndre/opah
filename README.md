# Senior Developer Technical Assessment — Debugging & Code Review

## Objective
[cite_start]To review, debug, and propose improvements for two code snippets (one in Java, one in C#) containing intentional issues related to **concurrency**, **resource management**, and **asynchronous execution**[cite: 57, 58, 59].

## Project Structure
The corrected files are organized as follows:
```

opah/
├── csharp/
│   └── DownloaderApp/
│       └── Downloader.cs
└── java/
└── FileProcessor.java
```
---

## Part 1: Java Snippet Analysis and Fixes (`FileProcessor.java`)

### Identified Problems and Criticality

| Problem | Criticality in a Real-World System                                                                                                                                                                                                                                                             |
| :--- |:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Thread Safety / Concurrency Issue** | The original code used a shared, non-synchronized ArrayList<String> across multiple threads. This would cause race conditions and potentially corrupt program state.                                                                                                                           |
| **Resource Leakage** | `BufferedReader` was closed manually. If an exception occurred before `br.close()`,the file descriptor would remain open, leading to a resource leak. |
| **I/O Inefficiency** | Multiple threads re-opened and re-read the same `data.txt` file, resulting in wasted I/O and redundant computation.                                                                                                                                                            |
| **Missing Task Synchronization** | The program shut down the `ExecutorService` without waiting for all tasks to complete, so `lines.size()` could be printed before processing finished.                                                                         |
| **Poor Error Handling** | A generic `catch (Exception e)` inside each worker only printed stack traces. This practice hides the root cause of failures and complicates debugging.                                                                                                     |
| **Memory Growth** | All lines were stored in memory without limits. For large files, this can trigger OutOfMemoryError.                                                                                                     |

### Solution Applied: **ExecutorService with Try-with-Resources**

Instead of manually managing concurrency with unsafe collections, the solution uses:

* **Safe Concurrency:** A `ConcurrentLinkedQueue` stores results without risk of race conditions.
* **Resource Safety:** Both `ExecutorService` and `BufferedReader` are wrapped in `try-with-resources`, ensuring proper closure even in case of exceptions.
* **Task Synchronization:** The program explicitly waits for all submitted tasks via `awaitTermination`, guaranteeing correct output.
* **Cleaner Code:** Exception handling is consolidated, and shared mutable state is avoided.

---

## Part 2: C\# Snippet Analysis and Fixes (`Downloader.cs`)

### Identified Problems and Criticality

| Problem | Criticality in a Real-World System |
| :--- | :--- |
| **Asynchronous Flow / "Fire-and-Forget"** | The `DownloadAsync` method was called without being `await`ed, and its `Task` was not captured. [cite_start]The main program flow continued, causing the application to print "Cache size: 0" and **exit before any downloads completed**[cite: 44, 47]. |
| **Thread Safety / Concurrency Issue** | The static `List<string> cache` is **not thread-safe**. [cite_start]Concurrent calls to `cache.Add(data)` from multiple asynchronous operations would lead to **data corruption** and potential runtime exceptions[cite: 39, 52]. |
| **Resource Exhaustion (HttpClient)** | A new `HttpClient` was created inside a `using` block for **every single request**. [cite_start]Under load, this rapid creation and disposal of `HttpClient` instances leads to **socket exhaustion** (Port Exhaustion), crippling network operations system-wide[cite: 50]. |
| **Missing Error Handling** | Any network-related exception (timeout, 404, DNS failure) would cause the individual `Task` to fail, but the errors were **not logged or gracefully handled**. |

### Solution Applied: **Asynchronous Task Management and Resource Reutilization**

The fix focuses on proper **asynchronous task collection and safe, shared resources**.

* **Concurrency Fix:** Replaced `List<string>` with **`ConcurrentBag<string>`** for thread-safe additions.
* **Resource Fix:** The `HttpClient` is now a single, **static and shared** instance, preventing socket exhaustion. A `Timeout` was also added for robustness.
* **Async Flow Fix:** All tasks are collected into a `List<Task>` and managed with **`await Task.WhenAll(tasks)`** to ensure the main method waits for all downloads to successfully complete or fail before proceeding.
* **Error Handling:** Added a **`try-catch`** block to `DownloadAsync` to log specific download failures, allowing the remaining tasks to continue processing.

---

## How to Execute the Examples

### 1. Java Execution

1.  Place a file named **`data.txt`** in the `java/` directory (alongside `FileProcessor.java`).
2.  Compile the code:
    ```bash
    javac java/FileProcessor.java
    ```
3.  Execute the compiled class:
    ```bash
    java -cp java FileProcessor
    ```

### 2. C\# Execution

1.  Navigate to the project directory:
    ```bash
    cd csharp/DownloaderApp
    ```
2.  Run the application using the .NET CLI:
    ```bash
    dotnet run
    ```
*(Note: Requires the .NET SDK to be installed.)*
