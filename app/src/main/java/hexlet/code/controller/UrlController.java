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
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static io.javalin.rendering.template.TemplateUtil.model;

public class UrlController {
    public static void create(Context ctx) throws SQLException {
        var url = ctx.formParamAsClass("url", String.class)
                .check(value -> !value.isEmpty(), "Некорректный URL")
                .get();
        try {
            var formattedUrl = formatUrl(url);
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
        } catch (IllegalArgumentException | URISyntaxException e) {
            var page = new BasePage();
            page.setFlash("Некорректный URL");
            page.setFlashType("danger");
            ctx.render("root.jte", model("page", page));
        }
    }

    public static void index(Context ctx) throws SQLException {
        var urls = UrlRepository.getEntities();
        var page = new UrlsPage(urls);
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

    public static void checkUrl(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url not found"));
        try {
            var response = Unirest.get(url.getName()).asString();
            var statusCode = response.getStatus();
            var body = Jsoup.parse(response.getBody());
            var title = body.title();
            var h1 = body.selectFirst("h1") == null ? null : body.selectFirst("h1").text();
            var description = body.selectFirst("meta[name=description]") == null ? null
                    : body.selectFirst("meta[name=description]").attr("content");
            var urlCheck = new UrlCheck(statusCode, title, h1, description, id);
            UrlChecksRepository.save(urlCheck);
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flashType", "success");
            ctx.status(200);
            ctx.redirect(NamedRoutes.urlPath(id));
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flashType", "danger");
            ctx.status(400);
            ctx.redirect(NamedRoutes.urlPath(id));
        }
    }

    public static String formatUrl(String url) throws URISyntaxException {
        var uri = new URI(url).normalize();
        try {
            uri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(uri.getScheme())
               .append("://")
               .append(uri.getHost());

        if (uri.getPort() != -1) {
            builder.append(":")
                   .append(uri.getPort());
        }
        return builder.toString();
    }
}
