package managers;

import enums.UserAccess;
import models.Cart;
import models.User;
import org.joda.time.DateTime;
import org.mindrot.jbcrypt.BCrypt;
import play.Logger;
import play.db.jpa.JPA;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class Users {

    public static User byId(Long id) {
        Logger.debug("Looking for User with ID {}", id);
        return JPA.em().find(User.class, id);
    }

    public static User byEmail(String email) {
        Logger.debug("Looking for User with email {}", email);
        String q = "SELECT u FROM User u WHERE u.email = :email";

        TypedQuery<User> query = JPA.em().createQuery(q, User.class);
        query.setParameter("email", email);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            Logger.debug("User not found.");
            return null;
        }

    }

    public static String authenticate(String email, String plain_password) {
        Logger.debug("Users.authenticate: looking for email: {}, password: {}", email, plain_password);
        String q = "SELECT u FROM User u WHERE u.email = :email";

        TypedQuery<User> query = JPA.em().createQuery(q, User.class);
        try {
            User candidate = query.setParameter("email", email).getSingleResult();
            return BCrypt.checkpw(plain_password, candidate.password) ? null : "The email and password didn't match";
        } catch (NoResultException e) {
            return "There is no user with that email";
        }
    }

    public static User save(User signup) {
        Logger.debug("Saving new User: {}", signup);
        String hashed_password = BCrypt.hashpw(signup.password, BCrypt.gensalt());
        DateTime now = DateTime.now();

        signup.signedUp = now.toDate();
        signup.password = hashed_password;
        if (signup.userAccess == null) {
            signup.userAccess = UserAccess.USER;
        }
        signup.cart = Cartz.create();
        JPA.em().persist(signup);

        return signup;
    }

    public static User update(User user) {
        Logger.debug("Updating user: {}", user);
        JPA.em().merge(user);
        return user;
    }

    public static List<User> list(Integer offset, Integer limit) {
        if (offset == null) offset = 10;
        if (limit == null) limit = 10;
        Logger.debug("Listing users ({}, {})", offset, limit);
        String q = "SELECT u FROM User u";

        TypedQuery<User> query = JPA.em().createQuery(q, User.class);
        List<User> result = query.setFirstResult(offset)
                                 .setMaxResults(limit)
                                 .getResultList();
        return result;
    }

    public static int count() {
        String q = "SELECT count(u) FROM User u";

        return JPA.em().createQuery(q, Long.class).getSingleResult().intValue();
    }

    public static String getCookieVal(String email) {
        return BCrypt.hashpw(email, BCrypt.gensalt());
    }

    public static boolean checkCookieVal(String email, String cookie) {
        return BCrypt.checkpw(email, cookie);
    }

    public static boolean isAdmin(String email) {
        Logger.debug("Users.isAdmin for email {}", email);
        String q = "SELECT COUNT(u) FROM User u WHERE " +
                   "email = :email AND userAccess = :userAccess";

        Long count = JPA.em().createQuery(q, Long.class)
                .setParameter("email", email)
                .setParameter("userAccess", UserAccess.ADMIN)
                .getSingleResult();

        if (count == 1) {
            return true;
        }
        return false;
    }
}
