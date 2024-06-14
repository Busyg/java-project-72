package hexlet.code.repository;

import hexlet.code.model.UrlCheck;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrlChecksRepository extends BaseRepository {
    public static void save(UrlCheck urlCheck) throws SQLException {
        String statement = "INSERT INTO url_checks (status_code, title, h1, description, url_id, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?);";
        try (
                var conn = dataSource.getConnection();
                var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)
        ) {
            urlCheck.setCreatedAt(Timestamp.from(ZonedDateTime.now().toInstant()));
            preparedStatement.setInt(1, urlCheck.getStatusCode());
            preparedStatement.setString(2, urlCheck.getTitle());
            preparedStatement.setString(3, urlCheck.getH1());
            preparedStatement.setString(4, urlCheck.getDescription());
            preparedStatement.setLong(5, urlCheck.getUrlId());
            preparedStatement.setTimestamp(6, urlCheck.getCreatedAt());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                urlCheck.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static List<UrlCheck> getUrlChecks(Long id) throws SQLException {
        String statement = "SELECT * FROM url_checks WHERE url_id = ?;";
        try (
                var conn = dataSource.getConnection();
                var preparedStatement = conn.prepareStatement(statement)
        ) {
            preparedStatement.setLong(1, id);
            var resultSet = preparedStatement.executeQuery();
            List<UrlCheck> urlChecks = new ArrayList<>();
            while (resultSet.next()) {
                var urlCheck = mapUrlCheck(resultSet);
                urlChecks.add(urlCheck);
            }
            return urlChecks;
        }
    }

    public static Map<Long, UrlCheck> getLastUrlChecks() throws SQLException {
        String statement = "SELECT DISTINCT ON (url_id) * FROM url_checks "
                + "ORDER BY url_id DESC;";
        try (
                var conn = dataSource.getConnection();
                var preparedStatement = conn.prepareStatement(statement)
        ) {
            var resultSet = preparedStatement.executeQuery();
            Map<Long, UrlCheck> resultMap = new HashMap<>();

            while (resultSet.next()) {
                var urlCheck = mapUrlCheck(resultSet);
                resultMap.put(urlCheck.getUrlId(), urlCheck);
            }
            return resultMap;
        }
    }

    public static UrlCheck mapUrlCheck(ResultSet resultSet) throws SQLException {
        var urlCheckId = resultSet.getLong("id");
        var statusCode = resultSet.getInt("status_code");
        var title = resultSet.getString("title");
        var h1 = resultSet.getString("h1");
        var description = resultSet.getString("description");
        var urlId = resultSet.getLong("url_id");
        var createdAt = resultSet.getTimestamp("created_at");
        var urlCheck = new UrlCheck(statusCode, title, h1, description, urlId);
        urlCheck.setId(urlCheckId);
        urlCheck.setCreatedAt(createdAt);
        return urlCheck;
    }
}
