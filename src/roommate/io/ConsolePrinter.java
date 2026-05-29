package roommate.io;

import roommate.model.MatchResult;
import roommate.model.Person;

import java.util.List;

public class ConsolePrinter {
    public void print(List<MatchResult> results, List<Person> people) {
        if (results == null || results.isEmpty()) {
            System.out.println("매칭 결과가 없습니다.");
            return;
        }

        double sum = 0.0;
        double worst = Double.NEGATIVE_INFINITY;

        System.out.println("============================================");
        System.out.println("        룸메이트 매칭 결과 (요약)");
        System.out.println("============================================");
        System.out.println("응답자 수 : " + (people == null ? 0 : people.size()));
        System.out.println("페어 수   : " + results.size());
        System.out.println();

        int index = 1;
        for (MatchResult result : results) {
            System.out.printf("페어 %2d : ID %3d <--> ID %3d | 페널티 %.3f%n",
                    index++, result.personA, result.personB, result.totalPenalty);
            sum += result.totalPenalty;
            worst = Math.max(worst, result.totalPenalty);
        }

        System.out.println();
        System.out.printf("평균 페널티 : %.3f%n", sum / results.size());
        System.out.printf("최악 페어   : %.3f%n", worst);
        System.out.println("상세 내용은 result.txt 파일을 확인하세요.");
    }
}
