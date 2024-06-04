package hexlet.code.model;

import hexlet.code.repository.UrlChecksRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public class Url {
    private Long id;
    private String name;
    private Timestamp createdAt;

    public Url(String name) {
        this.name = name;
    }

    public final UrlCheck getLastUrlCheck() {
        UrlCheck lastCheck;
        try {
            lastCheck = UrlChecksRepository.getLastUrlCheck(id);
        } catch (Exception e) {
            lastCheck = null;
        }
        return lastCheck;
    }
}
