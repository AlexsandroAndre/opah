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

| Problem | Criticality in a Real-World System |
| :--- | :--- |
| **Thread Safety / Concurrency Issue** | Accessing the static, non-synchronized `ArrayList<String> lines` from multiple threads leads to a **Race Condition**. [cite_start]This guarantees **data corruption** (missing lines, inconsistent state) and can result in `ArrayIndexOutOfBoundsException`s in production[cite: 14]. |
| **Resource Leakage** | The `BufferedReader` was not wrapped in a **try-with-resources** block. [cite_start]If an `IOException` occurred during `br.readLine()`, the `br.close()` call would be skipped, leading to a file handle **resource leak** that could exhaust OS limits over time[cite: 20, 26]. |
| **I/O Inefficiency** | 10 separate threads were created to **re-open and read the same `data.txt` file**, causing massive I/O overhead and redundant work. |
| **Missing Task Synchronization** | The main thread called `executor.shutdown()` but didn't wait (`awaitTermination`) for the tasks to finish. [cite_start]The application could exit prematurely, printing an incomplete `lines.size()` result[cite: 34]. |
| **Poor Error Handling** | [cite_start]The general `catch (Exception e) {}` inside the lambda silently **swallows all exceptions**, making debugging impossible and masking critical I/O or other failures[cite: 29]. |

### Solution Applied: **Functional Parallel Processing**

The shared state and `ExecutorService` were eliminated in favor of Java's modern **Streams API** for safe and efficient parallelism.

* **Concurrency Fix:** The logic uses `Files.lines(...).parallel().map(...).toList()`, which is an **immutable, functional pipeline**. The **`parallel()`** method safely handles concurrent processing without requiring manual locks or external `List` synchronization.
* **Resource Fix:** `Files.lines()` is a form of managed resource and, when used within a `try-with-resources` block, **guarantees the underlying file resource is closed** upon completion or failure.
* **I/O Fix:** The file is **opened and read only once**.

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
