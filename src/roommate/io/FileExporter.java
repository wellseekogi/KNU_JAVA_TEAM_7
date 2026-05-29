package roommate.io;

import roommate.model.MatchResult;
import roommate.model.Person;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileExporter {
    public void export(List<MatchResult> results, List<Person> people, String filePath) {
        Map<Integer, Person> peopleById = indexById(people);

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8)) {
            writeHeader(writer, results, people);
            int index = 1;
            for (MatchResult result : results) {
                Person a = peopleById.get(result.personA);
                Person b = peopleById.get(result.personB);
                writePair(writer, index++, result, a, b);
            }
            writeSummary(writer, results);
        } catch (IOException e) {
            throw new IllegalStateException("결과 파일을 쓸 수 없습니다: " + filePath, e);
        }
    }

    private void writeHeader(BufferedWriter writer, List<MatchResult> results, List<Person> people)
            throws IOException {
        writer.write("==================================================");
        writer.newLine();
        writer.write("        룸메이트 매칭 결과 (상세)");
        writer.newLine();
        writer.write("==================================================");
        writer.newLine();
        writer.write("응답자 수 : " + (people == null ? 0 : people.size()));
        writer.newLine();
        writer.write("페어 수   : " + results.size());
        writer.newLine();
        writer.newLine();
    }

    private void writePair(BufferedWriter writer, int index, MatchResult result, Person a, Person b)
            throws IOException {
        writer.write(String.format("[페어 %d] ID %d <--> ID %d", index, result.personA, result.personB));
        writer.newLine();
        writer.write(String.format("총 페널티 : %.4f", result.totalPenalty));
        writer.newLine();
        writePenaltyByItem(writer, result);
        writeValueComparison(writer, a, b);
        writer.write("--------------------------------------------------");
        writer.newLine();
        writer.newLine();
    }

    private void writePenaltyByItem(BufferedWriter writer, MatchResult result) throws IOException {
        if (result.penaltyByItem == null || result.penaltyByItem.isEmpty()) {
            return;
        }
        writer.write("항목별 페널티");
        writer.newLine();
        for (Map.Entry<String, Double> entry : result.penaltyByItem.entrySet()) {
            writer.write(String.format("  %-10s : %.4f", entry.getKey(), entry.getValue()));
            writer.newLine();
        }
    }

    private void writeValueComparison(BufferedWriter writer, Person a, Person b) throws IOException {
        if (a == null || b == null) {
            return;
        }
        writer.newLine();
        writer.write("원본 응답값 비교");
        writer.newLine();
        writer.write(String.format("  %-18s | %-10s | %-10s", "항목", "A(" + a.id + ")", "B(" + b.id + ")"));
        writer.newLine();
        writer.write("  ---------------------------------------------");
        writer.newLine();
        writeRow(writer, "gender", String.valueOf(a.gender), String.valueOf(b.gender));
        writeRow(writer, "smoking", String.valueOf(a.smoking), String.valueOf(b.smoking));
        writeRow(writer, "smoking_pref", String.valueOf(a.smokingPref), String.valueOf(b.smokingPref));
        writeRow(writer, "allergy", a.allergy, b.allergy);
        writeRow(writer, "sleep_time", String.valueOf(a.sleepTime), String.valueOf(b.sleepTime));
        writeRow(writer, "wake_time", String.valueOf(a.wakeTime), String.valueOf(b.wakeTime));
        writeRow(writer, "cleaning", String.valueOf(a.cleaning), String.valueOf(b.cleaning));
        writeRow(writer, "temperature", String.valueOf(a.temperature), String.valueOf(b.temperature));
        writeRow(writer, "noise_tolerance", String.valueOf(a.noiseTolerance), String.valueOf(b.noiseTolerance));
        writeRow(writer, "study_place", a.studyPlace, b.studyPlace);
        writeRow(writer, "mbti_ei", String.valueOf(a.mbtiEI), String.valueOf(b.mbtiEI));
        writer.newLine();
    }

    private void writeRow(BufferedWriter writer, String label, String a, String b) throws IOException {
        writer.write(String.format("  %-18s | %-10s | %-10s", label, a, b));
        writer.newLine();
    }

    private void writeSummary(BufferedWriter writer, List<MatchResult> results) throws IOException {
        if (results.isEmpty()) {
            return;
        }
        double sum = 0.0;
        double best = Double.POSITIVE_INFINITY;
        double worst = Double.NEGATIVE_INFINITY;
        for (MatchResult result : results) {
            sum += result.totalPenalty;
            best = Math.min(best, result.totalPenalty);
            worst = Math.max(worst, result.totalPenalty);
        }
        writer.write("==================================================");
        writer.newLine();
        writer.write("요약");
        writer.newLine();
        writer.write(String.format("평균 페널티 : %.4f", sum / results.size()));
        writer.newLine();
        writer.write(String.format("최저 페어   : %.4f", best));
        writer.newLine();
        writer.write(String.format("최악 페어   : %.4f", worst));
        writer.newLine();
        writer.write("==================================================");
        writer.newLine();
    }

    private Map<Integer, Person> indexById(List<Person> people) {
        Map<Integer, Person> result = new HashMap<>();
        if (people == null) {
            return result;
        }
        for (Person person : people) {
            result.put(person.id, person);
        }
        return result;
    }
}
