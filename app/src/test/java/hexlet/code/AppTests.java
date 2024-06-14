package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlChecksRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTests {
    private Javalin app;

    @BeforeEach
    public final void setUp() {
        app = App.getApp();
    }

    @Test
    public void testRootPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.rootPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body()).isNotNull();
            assertThat(response.body().string()).contains("Анализатор страниц");
        });
    }

    @Test
    public void testURLsPage() {
        JavalinTest.test(app, (server, client) -> {
            UrlRepository.save(new Url("https://some-domain.org:8080"));
            var response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body()).isNotNull();
            assertThat(response.body().string()).contains("https://some-domain.org:8080");
        });
    }

    @Test
    public void testSaveUrl() {
        JavalinTest.test(app, (server, client) -> {
            try (var response = client.post(NamedRoutes.urlsPath(), "url=https://some-domain.org:8080/example/path")) {
                assertThat(response.code()).isEqualTo(200);
            }
            var savedUrl = client.get(NamedRoutes.urlPath("1"));
            assertThat(savedUrl.code()).isEqualTo(200);
            assertThat(UrlRepository.find(1L).get().getName()).isEqualTo("https://some-domain.org:8080");
        });
    }

    @Test
    public void testSaveBadUrl() {
        JavalinTest.test(app, (server, client) -> {
            try (var response = client.post(NamedRoutes.urlsPath(), "url=www.фывфыв")) {
                assertThat(response.code()).isEqualTo(200);
                assertThat(response.body()).isNotNull();
                assertThat(response.body().string()).contains("Некорректный URL");
            }
            var savedUrl = client.get(NamedRoutes.urlPath("1"));
            assertThat(savedUrl.code()).isEqualTo(404);
            assertThat(UrlRepository.getEntities().size()).isEqualTo(0);
        });
    }

    @Test
    public void testGetUrlById() {
        JavalinTest.test(app, (server, client) -> {
            UrlRepository.save(new Url("https://some-domain.org:8080"));
            var savedUrl = client.get(NamedRoutes.urlPath("1"));
            assertThat(savedUrl.code()).isEqualTo(200);
            assertThat(savedUrl.body()).isNotNull();
            assertThat(savedUrl.body().string()).contains("https://some-domain.org:8080");
        });
    }

    @Test
    public void testUrlChecks() throws IOException {
        var mockWebServer = new MockWebServer();
        var mockResponse = new MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(Files.readString(Path.of("./src/test/resources/mockPage.html")));
        mockWebServer.enqueue(mockResponse);
        mockWebServer.start();
        var url = mockWebServer.url("/").toString();
        JavalinTest.test(app, (server, client) -> {
            UrlRepository.save(new Url(url));
            try (var urlCheckResponse = client.post(NamedRoutes.urlChecksPath("1"))) {
                assertThat(urlCheckResponse.code()).isEqualTo(200);
            }
            var lastUrlChecks = UrlChecksRepository.getLastUrlChecks();
            var lastCheck = lastUrlChecks.get(1L);
            assertThat(lastCheck.getStatusCode()).isEqualTo(200);
            assertThat(lastCheck.getTitle()).isEqualTo("Title");
            assertThat(lastCheck.getDescription()).isEqualTo("Content");
            assertThat(lastCheck.getH1()).isEqualTo("Hello World!");
        });
        mockWebServer.shutdown();
    }
}
