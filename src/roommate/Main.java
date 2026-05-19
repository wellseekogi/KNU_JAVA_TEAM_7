package roommate;

import java.util.List;

import roommate.distance.DistanceRegistry;
import roommate.filter.HardFilterImpl;
import roommate.io.CsvReaderImpl;
import roommate.io.ResultWriterImpl;
import roommate.match.PenaltyCalculatorImpl;
import roommate.match.StableRoommatesMatcher;
import roommate.model.MatchResult;
import roommate.model.Person;

/**
 * 전체 흐름 조립용 진입점. (멤버 4)
 *
 *   1) CSV 읽어서 Person 리스트 만들기      (멤버 1)
 *   2) Hard 필터 / 거리 함수 / 점수 계산기 준비
 *   3) Stable Roommates 매칭                (멤버 3)
 *   4) 결과 콘솔/파일 출력                  (멤버 4)
 *
 * 실행 예시:
 *   java -cp out roommate.Main data/input_large.csv result.txt
 */
public class Main {

    public static void main(String[] args) {
        String inputPath  = (args.length > 0) ? args[0] : "data/input_large.csv";
        String outputPath = (args.length > 1) ? args[1] : "result.txt";

        // 1. CSV 읽기 (멤버 1)
        CsvReaderImpl reader = new CsvReaderImpl();
        List<Person> people = reader.read(inputPath);

        // 2. 의존성 조립
        HardFilterImpl filter = new HardFilterImpl();
        DistanceRegistry registry = new DistanceRegistry();
        PenaltyCalculatorImpl calculator = new PenaltyCalculatorImpl(registry);

        // 3. 매칭 (멤버 3)
        StableRoommatesMatcher matcher =
                new StableRoommatesMatcher(filter, calculator);
        List<MatchResult> results = matcher.match(people);

        // 4. 출력 (멤버 4)
        ResultWriterImpl writer = new ResultWriterImpl();
        writer.printToConsole(results, people);
        writer.writeToFile(results, people, outputPath);
    }
}
