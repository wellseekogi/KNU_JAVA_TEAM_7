package roommate.io;

import roommate.model.MatchResult;
import roommate.model.Person;

import java.util.List;

public interface ResultWriter {
    void printToConsole(List<MatchResult> results, List<Person> people);

    void writeToFile(List<MatchResult> results, List<Person> people, String filePath);
}
