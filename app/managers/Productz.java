package managers;

import enums.ProductType;
import models.Product;
import play.Logger;
import play.db.jpa.JPA;

import javax.persistence.TypedQuery;
import java.util.List;

public class Productz {

    public static Product byId(Long id) {
        return JPA.em().find(Product.class, id);
    }

    public static Product save(Product product) {
        Logger.debug("Saving new Product, name: {}, type: {}", product.name, product.type);
        JPA.em().persist(product);
        return product;
    }

    public static void delete(Product product) {
        JPA.em().remove(product);
    }

    public static Product deactivate(Product product) {
        product.active = false;
        JPA.em().merge(product);
        return product;
    }

    public static Product update(Product product) {
        JPA.em().merge(product);
        return product;
    }

    public static List<Product> list(Integer offset, Integer limit) {
        if (offset == null) offset = 10;
        if (limit == null) limit = 10;

        String q = "SELECT p FROM Product p";

        List<Product> products = JPA.em().createQuery(q, Product.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        return products;
    }

    public static List<Product> all() {
        String q = "SELECT p FROM Product p";

        List<Product> products = JPA.em().createQuery(q, Product.class)
                .getResultList();

        return products;
    }

    public static Page page(int pageNum, int pageSize, String filter, String sortField, String order, String pType) {
        String totalQ = "SELECT COUNT(p) FROM Product p WHERE p.active = true AND lower(p.name) LIKE :filter";
        String productQ = "SELECT p FROM Product p WHERE p.active = true AND lower(p.name) LIKE :filter";

        if (!pType.isEmpty()) {
            productQ += " AND p.type = :type";
            totalQ += " AND p.type = :type";
        }

        productQ += " ORDER BY p." + sortField + " " + order;

        TypedQuery<Long> totalQuery = JPA.em()
                .createQuery(totalQ, Long.class)
                .setParameter("filter", filter.toLowerCase() + "%");

        TypedQuery<Product> productQuery = JPA.em().createQuery(productQ, Product.class)
                .setParameter("filter", filter.toLowerCase() + "%");


        if (!pType.isEmpty()) {
            ProductType type = ProductType.valueOf(pType);
            totalQuery.setParameter("type", type);
            productQuery.setParameter("type", type);
        }

        int total = totalQuery.getSingleResult().intValue();
        System.out.println("TOTALLY TOTAL " + total);

        List<Product> data = productQuery.setFirstResult((pageNum - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        return new Page(data, total, pageNum, pageSize);
    }

    public static class Page {

        private final int pageSize;
        private final long totalRowCount;
        private final int pageIndex;
        private final List<Product> products;
        private final int totalPages;
        private final ProductType[] productChoices = ProductType.values();

        public Page(List<Product> data, int total, int pageNum, int pageSize) {
            System.out.println("TOTAL " + total);
            this.products = data;
            this.totalRowCount = total;
            this.pageIndex = pageNum;
            this.pageSize = pageSize;
            this.totalPages = (int) Math.ceil((float) total / pageSize);
        }

        public int getPageSize() {
            return pageSize;
        }

        public long getTotalRowCount() {
            return totalRowCount;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public List<Product> getProducts() {
            return products;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public ProductType[] getProductChoices() {
            return productChoices;
        }

    }

}
