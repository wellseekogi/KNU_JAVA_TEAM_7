package roommate.io;

import java.util.List;

import roommate.model.MatchResult;
import roommate.model.Person;

/**
 * ResultWriter 기본 구현체.
 *
 * 콘솔 출력과 파일 출력은 각각 ConsolePrinter / FileExporter 에 맡기고
 * 이 클래스는 단순히 호출만 한다.
 */
public class ResultWriterImpl implements ResultWriter {

    private final ConsolePrinter consolePrinter;
    private final FileExporter fileExporter;

    public ResultWriterImpl() {
        this.consolePrinter = new ConsolePrinter();
        this.fileExporter = new FileExporter();
    }

    @Override
    public void printToConsole(List<MatchResult> results, List<Person> people) {
        consolePrinter.print(results, people);
    }

    @Override
    public void writeToFile(List<MatchResult> results, List<Person> people, String filePath) {
        fileExporter.export(results, people, filePath);
    }
}
