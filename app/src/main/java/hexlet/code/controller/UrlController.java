package hexlet.code.controller;


import hexlet.code.dto.BasePage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlChecksRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import lombok.SneakyThrows;

import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public class UrlController {
    public static void create(Context ctx) throws SQLException {
        var url = ctx.formParamAsClass("url", String.class)
                .get();
        String formattedUrl;

        try {
            formattedUrl = formatUrl(url);
        } catch (RuntimeException e) {
            var page = new BasePage();
            page.setFlash("Некорректный URL");
            page.setFlashType("danger");
            ctx.render("root.jte", model("page", page));
            return;
        }

        if (UrlRepository.findByName(formattedUrl).isPresent()) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect(NamedRoutes.urlsPath());
            return;
        }

        UrlRepository.save(new Url(formattedUrl));
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect(NamedRoutes.urlsPath());
    }

    public static void index(Context ctx) throws SQLException {
        Map<Url, UrlCheck> urlsMap = new HashMap<>();
        Map<Long, UrlCheck> lastUrlChecks = UrlChecksRepository.getLastUrlChecks();
        var urls = UrlRepository.getEntities();
        for (var url : urls) {
            urlsMap.put(url, lastUrlChecks.get(url.getId()));
        }
        var page = new UrlsPage(urlsMap);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/index.jte", model("page", page));
    }

    public static void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url not found"));
        var page = new UrlPage(url, UrlChecksRepository.getUrlChecks(id));
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/show.jte", model("page", page));
    }

    @SneakyThrows
    public static String formatUrl(String url) {
        var parsedUrl = new URI(url).toURL();
        return String.format(
                "%s://%s%s",
                parsedUrl.getProtocol(),
                parsedUrl.getHost(),
                parsedUrl.getPort() == -1 ? "" : ":" + parsedUrl.getPort()
        );
    }
}
