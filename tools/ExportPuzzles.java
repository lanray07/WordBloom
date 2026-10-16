import com.wordbloom.engine.Entry;
import com.wordbloom.engine.Library;
import com.wordbloom.engine.Puzzle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Exports the actual Kotlin generator output for content review, without Android. */
class ExportPuzzles {
    private static String quote(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    public static void main(String[] args) throws Exception {
        List<String> puzzles = new ArrayList<>();
        for (Puzzle puzzle : Library.INSTANCE.getPuzzles()) {
            List<String> entries = new ArrayList<>();
            for (Entry entry : puzzle.getEntries()) {
                entries.add("{\"word\":" + quote(entry.getWord()) + ",\"x\":"
                    + entry.getX() + ",\"y\":" + entry.getY()
                    + ",\"down\":" + entry.getDown() + "}");
            }
            String bonus = puzzle.getBonus().stream().sorted().map(ExportPuzzles::quote)
                .collect(Collectors.joining(","));
            puzzles.add("{\"id\":" + quote(puzzle.getId()) + ",\"letters\":"
                + quote(puzzle.getLetters()) + ",\"world\":" + puzzle.getWorld()
                + ",\"difficulty\":" + quote(puzzle.getDifficulty().name())
                + ",\"width\":" + puzzle.getWidth() + ",\"height\":" + puzzle.getHeight()
                + ",\"entries\":[" + String.join(",", entries) + "],\"bonus\":[" + bonus + "]}");
        }
        Path output = Path.of(args.length == 0 ? "content/puzzles-en.json" : args[0]);
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, "{\"locales\":[\"en-GB\",\"en-US\"],"
            + "\"review_status\":\"structurally validated; linguistic review pending\","
            + "\"puzzles\":[\n" + String.join(",\n", puzzles) + "\n]}\n");
        System.out.println("Exported " + puzzles.size() + " generated puzzle layouts to " + output);
    }
}
