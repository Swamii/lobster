package managers;

import models.Cart;
import models.CartItem;
import models.Invoice;
import play.db.jpa.JPA;

import java.util.ArrayList;
import java.util.List;

public class Invoices {

    public static Invoice buy(Cart cart) {
        Invoice invoice = new Invoice();
        invoice.user = cart.user;
        invoice.cartItems = new ArrayList<>();
        for (CartItem item : cart.cartItems) {
            invoice.cartItems.add(item);
        }
        invoice.billable = Cartz.getTotalCost(cart);
        Cartz.clear(cart);
        JPA.em().persist(invoice);
        return invoice;
    }

    public static List<Invoice> byUserEmail(String email) {
        String q = "SELECT i FROM Invoice i WHERE i.user.email = :email";

        return JPA.em().createQuery(q, Invoice.class)
            .setParameter("email", email)
            .getResultList();
    }

}
