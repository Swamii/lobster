package models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
public class Invoice {

    @Id
    @GeneratedValue
    public Long id;

    @OneToOne
    public User user;

    @OneToMany(fetch = FetchType.LAZY)
    public List<CartItem> cartItems;

    public Double billable;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Invoice comp = (Invoice) obj;
        if (this.id.equals(comp.id)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Cart: " + id + ". Owned by: " + user + ". Contains: " + cartItems;
    }

}
