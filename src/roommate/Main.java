package roommate;

import roommate.distance.DistanceRegistry;
import roommate.filter.HardFilterImpl;
import roommate.io.CsvReaderImpl;
import roommate.io.ResultWriterImpl;
import roommate.match.PenaltyCalculatorImpl;
import roommate.match.StableRoommatesMatcher;
import roommate.model.MatchResult;
import roommate.model.Person;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String inputPath = args.length > 0 ? args[0] : "data/input_small.csv";
        String outputPath = args.length > 1 ? args[1] : "result.txt";

        try {
            CsvReaderImpl reader = new CsvReaderImpl();
            List<Person> people = reader.read(inputPath);

            HardFilterImpl filter = new HardFilterImpl();
            DistanceRegistry registry = new DistanceRegistry();
            PenaltyCalculatorImpl calculator = new PenaltyCalculatorImpl(registry);
            StableRoommatesMatcher matcher = new StableRoommatesMatcher(filter, calculator);

            List<MatchResult> results = matcher.match(people);

            ResultWriterImpl writer = new ResultWriterImpl();
            writer.printToConsole(results, people);
            writer.writeToFile(results, people, outputPath);
        } catch (RuntimeException e) {
            System.err.println("[오류] " + e.getMessage());
            System.exit(1);
        }
    }
}
