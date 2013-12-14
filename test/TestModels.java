import enums.ProductType;
import managers.Users;
import models.CartItem;
import models.Product;
import models.User;
import org.junit.Test;
import play.db.jpa.JPA;
import play.test.WithApplication;

import java.util.HashSet;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class TestModels extends WithApplication {

    @Test
    public void testCRUDUser() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                JPA.withTransaction(new play.libs.F.Callback0() {
                    public void invoke() {
                        User user = new User();
                        user.email = "fake1@ok.com";
                        user.firstName = "Lars";
                        user.password = "fake_password";
                        Users.save(user);
                        assertThat(user.signedUp).isNotNull();
                    }
                });
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void testBadUser() {
        /**
         * No EMAIL!!!
         */
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                JPA.withTransaction(new play.libs.F.Callback0() {
                    public void invoke() {
                        User user = new User();
                        user.firstName = "Larssa";
                        Users.save(user);
                    }
                });
            }
        });
    }

    @Test
    public void testAuthenticate() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                JPA.withTransaction(new play.libs.F.Callback0() {
                    public void invoke() {
                        User user = new User();
                        user.email = "hamana@hamana.com";
                        user.firstName = "Hamana Man!";
                        user.password = "123456";
                        Users.save(user);

                        assertThat(Users.authenticate("hamana@hamana.com", "123456"))
                                .isNull();
                        assertThat(Users.authenticate("hamana@hamana.com", "xxxxxx"))
                                .isEqualTo("The email and password didn't match");
                        assertThat(Users.authenticate("homini@ok.com", "123456"))
                                .isEqualTo("There is no user with that email");
                    }
                });
            }
        });
    }

    @Test
    public void testCreateCart() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                JPA.withTransaction(new play.libs.F.Callback0() {
                    public void invoke() {
                        User user = new User();
                        user.email = "fake1@ok.com";
                        user.firstName = "Lars";
                        Users.save(user);
                        assertThat(user.signedUp).isNotNull();
                    }
                });
            }
        });
    }

    @Test
    public void testCreateProduct() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                JPA.withTransaction(new play.libs.F.Callback0() {
                    public void invoke() {
                        User user = new User();
                        user.email = "walla@man.com";
                        user.firstName = "Lars";
                        user.lastName = "Test";
                        user = Users.save(user);
                        assertThat(user).isNotNull();
                        assertThat(user.cart).isNotNull();

                        user.cart.cartItems = new HashSet<CartItem>();

                        user = Users.update(user);

                        User wuda = Users.byId(user.id);

                        assertThat(wuda.cart.cartItems).isEmpty();
                        assertThat(wuda.cart.cartItems).isNotNull();

                        Product product = new Product();
                        product.name = "soppa";
                        product.price = 100;
                        product.type = ProductType.GENERIC_FISH;

                    }
                });
            }
        });
    }


}
