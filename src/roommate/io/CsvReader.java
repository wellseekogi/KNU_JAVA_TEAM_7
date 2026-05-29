package roommate.io;

import java.util.List;
import roommate.model.Person;

public interface CsvReader {
    List<Person> read(String filePath);
}
