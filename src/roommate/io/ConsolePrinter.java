package roommate.io;

import java.util.List;

import roommate.model.MatchResult;
import roommate.model.Person;

/**
 * 콘솔 출력 담당.
 *
 * 실행 직후 한눈에 확인할 수 있는 요약만 찍는다.
 * 자세한 내용은 result.txt 쪽(FileExporter)에서 본다.
 */
public class ConsolePrinter {

    public void print(List<MatchResult> results, List<Person> people) {
        if (results == null || results.isEmpty()) {
            System.out.println("매칭 결과가 없습니다.");
            return;
        }

        System.out.println("============================================");
        System.out.println("        룸메이트 매칭 결과 (요약)");
        System.out.println("============================================");
        System.out.println("응답자 수 : " + (people == null ? 0 : people.size()));
        System.out.println("페어 수   : " + results.size());
        System.out.println();

        // 페어 한 줄씩
        int idx = 1;
        double sum = 0.0;
        double worst = Double.NEGATIVE_INFINITY;
        for (MatchResult r : results) {
            System.out.printf("페어 %2d : ID %3d  <-->  ID %3d   |  페널티 %.3f%n",
                    idx, r.personA, r.personB, r.totalPenalty);
            sum += r.totalPenalty;
            if (r.totalPenalty > worst) {
                worst = r.totalPenalty;
            }
            idx++;
        }

        System.out.println();
        System.out.printf("평균 페널티 : %.3f%n", sum / results.size());
        System.out.printf("최악 페어   : %.3f%n", worst);
        System.out.println("상세 내용은 result.txt 를 확인하세요.");
    }
}
