package controllers;

import managers.Productz;
import models.Product;
import play.Logger;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.*;
import security.AdminReq;
import java.util.List;
import views.html.admin.productList;
import views.html.admin.productView;

@With(Auth.class)
@Security.Authenticated(AdminReq.class)
public class Admin extends Controller {

    @Transactional
    public static Result products() {
        List products = Productz.all();
        Form<Product> productForm = Form.form(Product.class);
        return ok(productList.render(products, productForm));
    }

    @Transactional
    public static Result productDetail(Long id) {
        Product product = Productz.byId(id);
        Form<Product> productForm = Form.form(Product.class).fill(product);
        return ok(productView.render(product, productForm));
    }

    @Transactional
    public static Result addProduct() {
        Form<Product> productForm = Form.form(Product.class).bindFromRequest();
        List products = Productz.all();
        if (productForm.hasErrors()) {
            return badRequest(productList.render(products, productForm));
        }

        Product product = productForm.get();
        Productz.save(product);

        return redirect(routes.Admin.products());
    }

    @Transactional
    public static Result editProduct(Long id) {
        Product oldProduct = Productz.byId(id);
        Form<Product> productForm = Form.form(Product.class).bindFromRequest();
        if (productForm.hasErrors()) {
            return badRequest(productView.render(oldProduct, productForm));
        }
        Product updProduct = productForm.get();
        updProduct.id = oldProduct.id;
        Logger.debug("Updaging {} TO {}", oldProduct, updProduct);
        Productz.update(updProduct);
        return redirect(routes.Admin.products());
    }
}
