package roommate.io;

import roommate.model.Person;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CsvReaderImpl implements CsvReader {
    private static final int COLUMN_COUNT = 21;

    @Override
    public List<Person> read(String filePath) {
        List<Person> people = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            boolean header = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (header) {
                    header = false;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                people.add(parsePerson(line, lineNumber));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("CSV 파일을 읽을 수 없습니다: " + filePath, e);
        }

        return people;
    }

    private Person parsePerson(String line, int lineNumber) {
        String[] tokens = line.split(",", -1);
        if (tokens.length != COLUMN_COUNT) {
            throw new IllegalArgumentException(
                    "CSV " + lineNumber + "번째 줄의 컬럼 수가 " + tokens.length + "개입니다. 필요한 컬럼 수: " + COLUMN_COUNT);
        }

        try {
            int index = 0;
            Person person = new Person();
            person.id = parseInt(tokens[index++], "id");
            person.gender = parseChar(tokens[index++], "gender");
            person.smoking = parseChar(tokens[index++], "smoking");
            person.smokingPref = parseChar(tokens[index++], "smoking_pref");
            person.allergy = parseString(tokens[index++], "allergy");
            person.sleepTime = parseInt(tokens[index++], "sleep_time");
            person.wakeTime = parseInt(tokens[index++], "wake_time");
            person.cleaning = parseInt(tokens[index++], "cleaning");
            person.temperature = parseInt(tokens[index++], "temperature");
            person.noiseTolerance = parseInt(tokens[index++], "noise_tolerance");
            person.studyPlace = parseString(tokens[index++], "study_place");
            person.mbtiEI = parseChar(tokens[index++], "mbti_ei");

            person.importance = new HashMap<>();
            person.importance.put("smoking", parseInt(tokens[index++], "w_smoking"));
            person.importance.put("allergy", parseInt(tokens[index++], "w_allergy"));
            person.importance.put("sleep", parseInt(tokens[index++], "w_sleep"));
            person.importance.put("wake", parseInt(tokens[index++], "w_wake"));
            person.importance.put("cleaning", parseInt(tokens[index++], "w_cleaning"));
            person.importance.put("temp", parseInt(tokens[index++], "w_temp"));
            person.importance.put("noise", parseInt(tokens[index++], "w_noise"));
            person.importance.put("study", parseInt(tokens[index++], "w_study"));
            person.importance.put("mbti", parseInt(tokens[index], "w_mbti"));

            validate(person, lineNumber);
            return person;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("CSV " + lineNumber + "번째 줄 파싱 실패: " + line, e);
        }
    }

    private int parseInt(String value, String column) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(column + " 값이 비어 있습니다.");
        }
        return Integer.parseInt(trimmed);
    }

    private char parseChar(String value, String column) {
        String trimmed = value.trim();
        if (trimmed.length() != 1) {
            throw new IllegalArgumentException(column + " 값은 한 글자여야 합니다.");
        }
        return trimmed.charAt(0);
    }

    private String parseString(String value, String column) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(column + " 값이 비어 있습니다.");
        }
        return trimmed;
    }

    private void validate(Person p, int lineNumber) {
        require(p.id >= 1, lineNumber, "id는 1 이상이어야 합니다.");
        require(p.gender == 'M' || p.gender == 'F', lineNumber, "gender는 M/F만 가능합니다.");
        require(p.smoking == 'Y' || p.smoking == 'N', lineNumber, "smoking은 Y/N만 가능합니다.");
        require(p.smokingPref == 'Y' || p.smokingPref == 'N' || p.smokingPref == 'X',
                lineNumber, "smoking_pref는 Y/N/X만 가능합니다.");
        require(isOneOf(p.allergy, "none", "dust", "pet", "food"),
                lineNumber, "allergy는 none/dust/pet/food만 가능합니다.");
        require(p.sleepTime >= 0 && p.sleepTime <= 28, lineNumber, "sleep_time은 0~28이어야 합니다.");
        require(p.wakeTime >= 5 && p.wakeTime <= 12, lineNumber, "wake_time은 5~12이어야 합니다.");
        require(p.cleaning >= 0 && p.cleaning <= 7, lineNumber, "cleaning은 0~7이어야 합니다.");
        require(p.temperature >= 18 && p.temperature <= 28, lineNumber, "temperature는 18~28이어야 합니다.");
        require(p.noiseTolerance >= 1 && p.noiseTolerance <= 5, lineNumber, "noise_tolerance는 1~5이어야 합니다.");
        require(isOneOf(p.studyPlace, "room", "library", "mixed"),
                lineNumber, "study_place는 room/library/mixed만 가능합니다.");
        require(p.mbtiEI == 'E' || p.mbtiEI == 'I', lineNumber, "mbti_ei는 E/I만 가능합니다.");

        for (int value : p.importance.values()) {
            require(value >= 1 && value <= 10, lineNumber, "중요도는 1~10이어야 합니다.");
        }
    }

    private boolean isOneOf(String value, String... candidates) {
        for (String candidate : candidates) {
            if (candidate.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private void require(boolean condition, int lineNumber, String message) {
        if (!condition) {
            throw new IllegalArgumentException("CSV " + lineNumber + "번째 줄 오류: " + message);
        }
    }
}
