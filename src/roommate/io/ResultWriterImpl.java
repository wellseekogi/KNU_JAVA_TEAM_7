package roommate.io;

import roommate.model.MatchResult;
import roommate.model.Person;

import java.util.List;

public class ResultWriterImpl implements ResultWriter {
    private final ConsolePrinter consolePrinter = new ConsolePrinter();
    private final FileExporter fileExporter = new FileExporter();

    @Override
    public void printToConsole(List<MatchResult> results, List<Person> people) {
        consolePrinter.print(results, people);
    }

    @Override
    public void writeToFile(List<MatchResult> results, List<Person> people, String filePath) {
        fileExporter.export(results, people, filePath);
    }
}
