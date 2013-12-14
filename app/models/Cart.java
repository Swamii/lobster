package models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue
    public Long id;

    @OneToOne(mappedBy = "cart")
    @PrimaryKeyJoinColumn
    @JsonIgnore
    public User user;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CartItem> cartItems;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Cart comp = (Cart) obj;
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