package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import managers.Users;
import enums.UserAccess;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="`user`")
public class User {

    @Id
    @GeneratedValue
    public Long id;

    @Constraints.Required
    @Constraints.Email
    @Column(unique = true)
    public String email;

    @Constraints.Required
    @Constraints.MinLength(3)
    public String firstName;

    public String lastName;

    @Constraints.Required
    @Constraints.MinLength(6)
    @JsonIgnore
    public String password;

    @Enumerated(EnumType.STRING)
    public UserAccess userAccess;

    public Date signedUp;

    @OneToOne(cascade = CascadeType.REMOVE)
    public Cart cart;

    public String validate() {
        if (null != Users.byEmail(email)) {
            return "That email already exists";
        }
        if (password.length() < 6) {
            return "The password needs to be 6 characters or more";
        }
        if (firstName.length() < 2) {
            return "You need to have a longer first name";
        }
        return null;
    }

    @Override
    public String toString() {
        return "User: " + this.id + " - " + this.email;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        User comp = (User) obj;
        if (this.id != null && comp.id != null && this.id.equals(comp.id)) {
            return true;
        }
        if (this.email != null && comp.email != null && this.email.equals(comp.email)) {
            return true;
        }
        return false;
    }
}
