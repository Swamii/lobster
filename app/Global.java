import managers.Productz;
import managers.Users;
import models.Product;
import models.User;
import play.Application;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.db.jpa.JPA;
import play.filters.csrf.CSRFFilter;
import play.libs.Yaml;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Global extends GlobalSettings {

    @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{CSRFFilter.class};
    }

    @Override
    public void onStart(Application app) {
        JPA.withTransaction(new play.libs.F.Callback0() {
            public void invoke() {
                // Check if db is empty
                if (Users.count() == 0) {
                    Map items = (Map) Yaml.load("initial-data.yml");
                    List users = (List) items.get("users");
                    List products = (List) items.get("products");

                    for (Object o : users) {
                        if (o instanceof User) {
                            User u = (User) o;
                            Users.save(u);
                        }
                    }

                    for (Object o : products) {
                        if (o instanceof Product) {
                            Product p = (Product) o;
                            Productz.save(p);
                        }
                    }

                }
            }
        });
    }
}
