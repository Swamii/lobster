package managers;

import models.Cart;
import models.CartItem;
import models.Product;
import models.User;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;

public class Cartz {

    public static Cart byId(Long id) {
        return JPA.em().find(Cart.class, id);
    }

    public static CartItem itemById(Long itemId) {
        return JPA.em().find(CartItem.class, itemId);
    }

    public static Cart create() {
        Cart cart = new Cart();
        JPA.em().persist(cart);
        return cart;
    }

    public static Cart update(String email, CartItem item) {
        Cart cart = Users.byEmail(email).cart;
        Logger.debug("Testing update with merge item, merge cart.");
        JPA.em().merge(item);
        Logger.debug("Cart update, before: {}", cart.cartItems);
        JPA.em().merge(cart);
        Logger.debug("Cart update, after: {}", cart.cartItems);
        return cart;
    }

    public static Cart removeItem(String email, Long itemId) {
        Cart cart = Users.byEmail(email).cart;
        CartItem rmItem = itemById(itemId);
        Logger.debug("Removing {} from {}. Count: {}. Check if it deleted correctly (orphan-ting)", rmItem, cart, cart.cartItems.size());
        cart.cartItems.remove(rmItem);
        JPA.em().merge(cart);
        Logger.debug("Size is now {}", cart.cartItems.size());
        return cart;
    }

    public static CartItem add(String email, Long productId) {
        Cart cart = Users.byEmail(email).cart;

        for (CartItem existingItem : cart.cartItems) {
            if (existingItem.product.id.equals(productId)) {
                return null;
            }
        }

        CartItem item = new CartItem();

        Product product = Productz.byId(productId);

        item.quantity = 1;
        item.product = product;

        JPA.em().persist(item);

        cart.cartItems.add(item);
        JPA.em().merge(cart);

        return item;
    }


    /* OLD OLD OLD -> */

    public static void delete(String sessionId) {
        Cart cart = find(sessionId);
        JPA.em().remove(cart);
        Cache.remove("cart." + sessionId);
    }

    public static Cart find(String sessionId) {
        Cart cart = (Cart) Cache.get("cart." + sessionId);
        if (cart == null) {
            cart = JPA.em().createQuery(
                    "SELECT c FROM Cart c WHERE c.sessionId = :sessionId",
                    Cart.class
            ).setParameter("sessionId", sessionId)
                    .getSingleResult();
            Cache.set("cart." + sessionId, cart);
        }
        return cart;
    }

}
