import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.*;

public class FileProcessor {

    public static void main(String[] args) {
        try (ExecutorService executor =
                     Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {

            Queue<String> lines = new ConcurrentLinkedQueue<>();

            try (BufferedReader br = Files.newBufferedReader(Paths.get("data.txt"), StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    final String currentLine = line;
                    executor.submit(() -> lines.add(currentLine.toUpperCase()));
                }
            }

            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }

            System.out.println("Lines processed: " + lines.size());

        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}


