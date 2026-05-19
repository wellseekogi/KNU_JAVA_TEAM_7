package roommate.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roommate.model.MatchResult;
import roommate.model.Person;

/**
 * result.txt 작성 담당.
 *
 * 페어마다
 *   - ID 와 총 페널티
 *   - 항목별 페널티 (MatchResult.penaltyByItem 가 채워져 있으면)
 *   - 두 사람의 원본 응답값 비교 표
 * 를 적어둔다.
 *
 * 멤버 3 이 penaltyByItem 을 안 채워서 null 로 보내도 통합에 지장 없도록 처리.
 */
public class FileExporter {

    public void export(List<MatchResult> results, List<Person> people, String filePath) {
        Map<Integer, Person> byId = buildIndex(people);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            writeHeader(bw, results, people);

            int idx = 1;
            for (MatchResult r : results) {
                Person a = byId.get(r.personA);
                Person b = byId.get(r.personB);

                writePairHeader(bw, idx, r);
                writePenaltyBreakdown(bw, r);
                writeValueComparison(bw, a, b);

                bw.write("--------------------------------------------------");
                bw.newLine();
                bw.newLine();
                idx++;
            }

            writeFooter(bw, results);

        } catch (IOException e) {
            // 파일 쓰기 실패는 치명적이진 않으므로 에러만 띄우고 끝낸다.
            System.err.println("[FileExporter] 결과 파일 쓰기 실패: " + e.getMessage());
        }
    }

    // ---------------- 내부 보조 메서드 ----------------

    private void writeHeader(BufferedWriter bw, List<MatchResult> results, List<Person> people)
            throws IOException {
        bw.write("==================================================");
        bw.newLine();
        bw.write("        룸메이트 매칭 결과 (상세)");
        bw.newLine();
        bw.write("==================================================");
        bw.newLine();
        bw.write("응답자 수 : " + (people == null ? 0 : people.size()));
        bw.newLine();
        bw.write("페어 수   : " + results.size());
        bw.newLine();
        bw.newLine();
    }

    private void writePairHeader(BufferedWriter bw, int idx, MatchResult r) throws IOException {
        bw.write(String.format("[페어 %d]  ID %d  <-->  ID %d", idx, r.personA, r.personB));
        bw.newLine();
        bw.write(String.format("  총 페널티 : %.4f", r.totalPenalty));
        bw.newLine();
    }

    private void writePenaltyBreakdown(BufferedWriter bw, MatchResult r) throws IOException {
        if (r.penaltyByItem == null || r.penaltyByItem.isEmpty()) {
            return;
        }
        bw.write("  - 항목별 페널티");
        bw.newLine();
        for (Map.Entry<String, Double> e : r.penaltyByItem.entrySet()) {
            bw.write(String.format("      %-10s : %.4f", e.getKey(), e.getValue()));
            bw.newLine();
        }
    }

    private void writeValueComparison(BufferedWriter bw, Person a, Person b) throws IOException {
        if (a == null || b == null) {
            return;
        }
        bw.newLine();
        bw.write("  - 원본 응답값 비교");
        bw.newLine();
        bw.write(String.format("      %-18s | %-10s | %-10s",
                "항목", "A(" + a.id + ")", "B(" + b.id + ")"));
        bw.newLine();
        bw.write("      ---------------------------------------------");
        bw.newLine();
        writeRow(bw, "gender",          String.valueOf(a.gender),      String.valueOf(b.gender));
        writeRow(bw, "smoking",         String.valueOf(a.smoking),     String.valueOf(b.smoking));
        writeRow(bw, "smoking_pref",    String.valueOf(a.smokingPref), String.valueOf(b.smokingPref));
        writeRow(bw, "allergy",         a.allergy,                     b.allergy);
        writeRow(bw, "sleep_time",      String.valueOf(a.sleepTime),   String.valueOf(b.sleepTime));
        writeRow(bw, "wake_time",       String.valueOf(a.wakeTime),    String.valueOf(b.wakeTime));
        writeRow(bw, "cleaning",        String.valueOf(a.cleaning),    String.valueOf(b.cleaning));
        writeRow(bw, "temperature",     String.valueOf(a.temperature), String.valueOf(b.temperature));
        writeRow(bw, "noise_tolerance", String.valueOf(a.noiseTolerance), String.valueOf(b.noiseTolerance));
        writeRow(bw, "study_place",     a.studyPlace,                  b.studyPlace);
        writeRow(bw, "mbti_ei",         String.valueOf(a.mbtiEI),      String.valueOf(b.mbtiEI));
        bw.newLine();
    }

    private void writeRow(BufferedWriter bw, String label, String va, String vb) throws IOException {
        bw.write(String.format("      %-18s | %-10s | %-10s", label, va, vb));
        bw.newLine();
    }

    private void writeFooter(BufferedWriter bw, List<MatchResult> results) throws IOException {
        if (results.isEmpty()) {
            return;
        }
        double sum = 0.0;
        double worst = Double.NEGATIVE_INFINITY;
        double best  = Double.POSITIVE_INFINITY;
        for (MatchResult r : results) {
            sum += r.totalPenalty;
            if (r.totalPenalty > worst) worst = r.totalPenalty;
            if (r.totalPenalty < best)  best  = r.totalPenalty;
        }
        bw.write("==================================================");
        bw.newLine();
        bw.write("  요약");
        bw.newLine();
        bw.write(String.format("    평균 페널티 : %.4f", sum / results.size()));
        bw.newLine();
        bw.write(String.format("    최저 페어   : %.4f", best));
        bw.newLine();
        bw.write(String.format("    최악 페어   : %.4f", worst));
        bw.newLine();
        bw.write("==================================================");
        bw.newLine();
    }

    private Map<Integer, Person> buildIndex(List<Person> people) {
        Map<Integer, Person> m = new HashMap<>();
        if (people == null) {
            return m;
        }
        for (Person p : people) {
            m.put(p.id, p);
        }
        return m;
    }
}
