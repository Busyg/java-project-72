package hexlet.code.controller;


import hexlet.code.dto.BasePage;
import io.javalin.http.Context;

import static io.javalin.rendering.template.TemplateUtil.model;

public class RootController {
    public static void index(Context ctx) {
        var page = new BasePage();
        ctx.render("root.jte", model("page", page));
    }
}
