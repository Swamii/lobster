package security;

import managers.Users;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class AdminReq extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context ctx) {
        String email = ctx.session().get("email");
        String hash = ctx.session().get("id");
        Logger.debug("AdminReq.getUsername: ", email, ": ", hash);
        if (Users.checkCookieVal(email, hash) && Users.isAdmin(email)) {
            return email;
        }
        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return forbidden();
    }
}
