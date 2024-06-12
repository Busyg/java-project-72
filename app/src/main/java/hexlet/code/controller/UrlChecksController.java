package hexlet.code.controller;

import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlChecksRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;

import java.sql.SQLException;

public class UrlChecksController {
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
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Страница не существует");
            ctx.sessionAttribute("flashType", "danger");
            ctx.status(400);
            ctx.redirect(NamedRoutes.urlPath(id));
        }
    }
}
