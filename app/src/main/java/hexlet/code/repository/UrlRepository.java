package hexlet.code.repository;

import hexlet.code.model.Url;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {
    public static void save(Url url) throws SQLException {
        String statement = "INSERT INTO urls (name, created_at) VALUES (?, ?);";
        try (
                var conn = dataSource.getConnection();
                var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)
        ) {
            url.setCreatedAt(Timestamp.from(ZonedDateTime.now().toInstant()));
            preparedStatement.setString(1, url.getName());
            preparedStatement.setTimestamp(2, url.getCreatedAt());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                url.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static Optional<Url> find(Long id) throws SQLException {
        String statement = "SELECT * FROM urls WHERE id = ?";
        try (
                var conn = dataSource.getConnection();
                var preparedStatement = conn.prepareStatement(statement)
        ) {
            preparedStatement.setLong(1, id);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                var url = new Url(id, name, createdAt);
                return Optional.of(url);
            }
            return Optional.empty();
        }
    }

    public static List<Url> getEntities() throws SQLException {
        String statement = "SELECT * FROM urls;";
        try (
                var conn = dataSource.getConnection();
                var preparedStatement = conn.prepareStatement(statement)
        ) {
            var resultSet = preparedStatement.executeQuery();
            List<Url> urls = new ArrayList<>();
            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                var url = new Url(id, name, createdAt);
                urls.add(url);
            }
            return urls;
        }
    }

    public static Optional<Url> findByName(String name) throws SQLException {
        String statement = "SELECT * FROM urls WHERE name = ?";
        try (
                var conn = dataSource.getConnection();
                var preparedStatement = conn.prepareStatement(statement)
        ) {
            preparedStatement.setString(1, name);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var id = resultSet.getLong("id");
                var createdAt = resultSet.getTimestamp("created_at");
                var url = new Url(id, name, createdAt);
                return Optional.of(url);
            }
            return Optional.empty();
        }
    }
}
