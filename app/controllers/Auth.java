package controllers;

import managers.Cartz;
import managers.Users;
import models.Cart;
import models.User;
import play.Logger;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

public class Auth extends Action.Simple {

    @Override
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        String email = ctx.session().get("user");
        String id = ctx.session().get("id");
        if (email != null && !email.isEmpty()) {
            ctx.args.put("user", Users.byEmail(email));
        }
        return delegate.call(ctx);
    }

    public static User getUser() {
        User loggedInUser = (User) Http.Context.current().args.get("user");
        return loggedInUser;
    }

    public static String getUsername() {
        User loggedInUser = (User) Http.Context.current().args.get("user");
        if (loggedInUser == null) {
            return "";
        }
        return loggedInUser.email;
    }

    public static boolean isAdmin() {
        User loggedInUser = (User) Http.Context.current().args.get("user");
        if (loggedInUser != null && Users.isAdmin(loggedInUser.email)) {
            return true;
        }
        return false;
    }
}
