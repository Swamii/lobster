package security;

import managers.Users;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class LoginReq extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context ctx) {
        String email = ctx.session().get("user");
        String hash = ctx.session().get("id");
        Logger.debug("LoginReq.getUsername: {}: {}", email, hash);
        if (Users.checkCookieVal(email, hash)) {
            return email;
        }
        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return forbidden("Please login to access this page");
    }
}
