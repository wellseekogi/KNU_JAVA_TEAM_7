package roommate.io;

import java.util.List;

import roommate.model.MatchResult;
import roommate.model.Person;

/**
 * 매칭 결과 출력 담당 인터페이스.
 *
 * 합의서 3.7 그대로 — 콘솔 요약 한 번, 텍스트 파일 한 번.
 * 구현체는 ResultWriterImpl 참고.
 */
public interface ResultWriter {

    /** 콘솔에 매칭 결과 요약 출력 */
    void printToConsole(List<MatchResult> results, List<Person> people);

    /** 상세 결과를 텍스트 파일로 저장 */
    void writeToFile(List<MatchResult> results, List<Person> people, String filePath);
}
