package models;

import enums.ProductType;
import play.data.validation.Constraints;

import javax.persistence.*;

@Entity
public class Product {

    @Id
    @GeneratedValue
    public Long id;

    @Constraints.Required
    public String name;

    public String description;

    @Constraints.Required
    public Integer price;

    @Constraints.Required
    @Enumerated(EnumType.STRING)
    public ProductType type;

    public boolean active;

    public String picture;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Product comp = (Product) obj;
        if (this.id.equals(comp.id)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Product " + id + ". Name: " + name + ". Price: " + price + ". Type: " + type;
    }
}
