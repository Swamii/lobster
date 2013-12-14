package controllers;

import managers.Users;

// -> Auth
public class Login {
    public String email;
    public String password;

    public String validate() {
        return Users.authenticate(email, password);
    }
}