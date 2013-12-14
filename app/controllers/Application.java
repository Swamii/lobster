package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import managers.Cartz;
import managers.Productz;
import managers.Users;
import models.*;
import play.Logger;
import play.Play;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import play.db.jpa.Transactional;
import play.db.jpa.JPA;
import security.CartOwnerReq;
import security.LoginReq;
import views.html.browse;
import views.html.index;
import views.html.auth.login;
import views.html.auth.signup;

@With(Auth.class)
public class Application extends Controller {

    @Transactional
    @Security.Authenticated(LoginReq.class)
    public static Result getCart() {
        User user = Users.byEmail(request().username());
        return ok(Json.toJson(user.cart));
    }

    @Transactional
    @Security.Authenticated(CartOwnerReq.class)
    public static Result addToCart() {
        JsonNode node = request().body().asJson();
        Long productId = node.findPath("id").asLong();
        CartItem item = Cartz.add(request().username(), productId);

        if (item != null) {
            return ok(Json.toJson(item));
        } else {
            Logger.warn("addToCart, item already in cart. node: {}", node);
            return badRequest("Item already in cart");
        }
    }

    @Transactional
    @Security.Authenticated(CartOwnerReq.class)
    public static Result updateCartItem(Long itemId) {
        CartItem item;
        ObjectMapper map = new ObjectMapper();
        try {
            item = map.readValue(request().body().asText(), CartItem.class);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.warn("updateCart, reading of -> {} <- failed.", request().body().asText());
            return badRequest("Bad input");
        }
        Cartz.update(request().username(), item);

        return ok(Json.toJson(item));
    }

    @Transactional
    @Security.Authenticated(CartOwnerReq.class)
    public static Result removeCartItem(Long itemId) {
        Cart cart = Cartz.removeItem(request().username(), itemId);
        return ok(Json.toJson(cart));
    }

    @Transactional
    public static Result index() {
        return ok(index.render());
    }

    @Transactional
    public static Result browse() {
        // TODO: User-email + has permission
        // From session-email -> api-req or template.
        return ok(browse.render());
    }

    @Transactional
    public static Result getProduct(Long id) {
        return Results.TODO;
    }

    @Transactional
    public static Result showSignup() {
        return ok(signup.render(Form.form(User.class)));
    }

    @Transactional
    public static Result signup() {
        Form<User> signupForm = Form.form(User.class).bindFromRequest();

        if (signupForm.hasErrors()) {
            return badRequest(signup.render(signupForm));
        } else {
            User user = signupForm.get();
            user.cart = new Cart();
            Users.save(user);
            return redirect(routes.Application.login());
        }
    }

    @Transactional
    public static Result login() {
        return ok(
            login.render(Form.form(Login.class))
        );
    }

    @Transactional
    public static Result authenticate() {
        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();

        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {
            String email = loginForm.get().email;
            session("id", Users.getCookieVal(email));
            session("user", email);

            return redirect(routes.Application.index());
        }
    }

    @Transactional
    public static Result logout() {
        session().clear();
        return redirect(routes.Application.index());
    }

    @Transactional(readOnly = true)
    public static Result getProducts(int page, int size, String sort, String order, String filter, String pType) {
        Productz.Page p = Productz.page(page, size, filter, sort, order, pType);
        Logger.debug("Constructed page: {}", p);
        return ok(Json.toJson(p));
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

    public static Result picture(String name) {
        String path = Play.application().configuration().getString("upload.path" + name);
        return ok(new File(path + name));
    }

}
