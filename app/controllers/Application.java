package controllers;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import play.db.jpa.Transactional;
import play.db.jpa.JPA;
import models.Dude;
import views.html.index;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Mah new application is ready"));
    }

    @Transactional
    public static Result addDude() {
        Form<Dude> form = Form.form(Dude.class).bindFromRequest();
        Dude dude = form.get();
        JPA.em().persist(dude);
        return ok(Json.toJson(dude));
    }

    @Transactional(readOnly = true)
    public static Result getDudes() {
        List dudes = JPA.em().createNamedQuery("Dude.findAll").getResultList();
        JsonNode jsonDudes = Json.toJson(dudes);
        return ok(jsonDudes);
    }

    @Transactional(readOnly = true)
    public static Result getDude(Long id) {
        Dude dude = JPA.em().find(Dude.class, id);
        return ok(Json.toJson(dude));
    }

    @Transactional
    public static Result updateDude(Long id) {
        Dude oldDude = JPA.em().find(Dude.class, id);
        Dude newDude = Form.form(Dude.class).bindFromRequest().get();
        oldDude.name = newDude.name;
        JPA.em().persist(oldDude);
        return ok(Json.toJson(newDude));
    }

    @Transactional
    public static Result deleteDude(Long id) {
        Dude dude = JPA.em().find(Dude.class, id);
        JPA.em().remove(dude);
        return ok("deleted");
    }
}
