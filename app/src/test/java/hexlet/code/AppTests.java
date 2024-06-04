package hexlet.code;

import hexlet.code.repository.UrlChecksRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTests {
    private Javalin app;
    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        var mockResponse = new MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody("<head><title>Title</title><meta name=" + "description" + " content=" + "Content" + ">"
                        + "<body><h1>Hello World!</h1></body>");
        mockWebServer.enqueue(mockResponse);
        mockWebServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    public void setUp() {
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
            var response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body()).isNotNull();
            assertThat(response.body().string()).contains("Сайты");
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
            assertThat(savedUrl.body()).isNotNull();
            assertThat(savedUrl.body().string()).contains("https://some-domain.org:8080");
        });
    }

    @Test
    public void testUrlChecks() {
        var url = mockWebServer.url("/").toString();
        JavalinTest.test(app, (server, client) -> {
            try (var urlResponse = client.post(NamedRoutes.urlsPath(), "url=" + url)) {
                assertThat(urlResponse.code()).isEqualTo(200);
            }
            try (var urlCheckResponse = client.post(NamedRoutes.urlChecksPath("1"))) {
                assertThat(urlCheckResponse.code()).isEqualTo(200);
            }
            var lastCheck = UrlChecksRepository.getLastUrlCheck(1L);
            assertThat(lastCheck.getTitle()).isEqualTo("Title");
            assertThat(lastCheck.getDescription()).isEqualTo("Content");
            assertThat(lastCheck.getH1()).isEqualTo("Hello World!");
        });
    }
}
