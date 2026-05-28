package roommate.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import roommate.model.Person;

public class CsvReaderImpl implements CsvReader {

    @Override
    public List<Person> read(String filePath) {
        List<Person> people = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // 헤더 건너뛰기
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] tokens = line.split(",");
                Person person = parsePerson(tokens);
                people.add(person);
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("파일 읽기 오류: " + filePath, e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자 파싱 오류", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("CSV 컬럼 수 불일치", e);
        }

        return people;
    }

    private Person parsePerson(String[] tokens) {
        Person person = new Person();

        int idx = 0;
        person.id = Integer.parseInt(tokens[idx++].trim());
        person.gender = tokens[idx++].trim().charAt(0);
        person.smoking = tokens[idx++].trim().charAt(0);
        person.smokingPref = tokens[idx++].trim().charAt(0);
        person.allergy = tokens[idx++].trim();
        person.sleepTime = Integer.parseInt(tokens[idx++].trim());
        person.wakeTime = Integer.parseInt(tokens[idx++].trim());
        person.cleaning = Integer.parseInt(tokens[idx++].trim());
        person.temperature = Integer.parseInt(tokens[idx++].trim());
        person.noiseTolerance = Integer.parseInt(tokens[idx++].trim());
        person.studyPlace = tokens[idx++].trim();
        person.mbtiEI = tokens[idx++].trim().charAt(0);

        // importance Map 생성 및 가중치 매핑
        person.importance = new HashMap<>();
        person.importance.put("smoking", Integer.parseInt(tokens[idx++].trim()));
        person.importance.put("allergy", Integer.parseInt(tokens[idx++].trim()));
        person.importance.put("sleep", Integer.parseInt(tokens[idx++].trim()));
        person.importance.put("wake", Integer.parseInt(tokens[idx++].trim()));
        person.importance.put("cleaning", Integer.parseInt(tokens[idx++].trim()));
        person.importance.put("temp", Integer.parseInt(tokens[idx++].trim()));
        person.importance.put("noise", Integer.parseInt(tokens[idx++].trim()));
        person.importance.put("study", Integer.parseInt(tokens[idx++].trim()));
        person.importance.put("mbti", Integer.parseInt(tokens[idx].trim()));

        return person;
    }
}
