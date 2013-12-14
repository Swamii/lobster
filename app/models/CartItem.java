package models;

import play.data.validation.Constraints;

import javax.persistence.*;

@Entity
public class CartItem {

    @Id
    @GeneratedValue
    public Long id;

    @Constraints.Required
    public Integer quantity;

    @ManyToOne
    public Product product;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        CartItem comp = (CartItem) obj;
        if (this.id.equals(comp.id)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CartItem " + id + ". Qty: " + quantity + ". Product: " + product;
    }
}
