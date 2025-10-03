import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.io.IOException;

public class FileProcessor {
    public static void main(String[] args) {
        Path path = Paths.get("data.txt");

        try (var lines = Files.lines(path, StandardCharsets.UTF_8)) {
            List<String> processed = lines
                    .parallel()
                    .map(String::toUpperCase)
                    .toList();

            System.out.println("Lines processed: " + processed.size());
        } catch (IOException e) {
            System.err.println("Failed to read file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
