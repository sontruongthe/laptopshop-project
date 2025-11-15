package module.Services.Impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import module.DAO.ProductDAO;
import module.Domain.Products;
import module.Services.ProductService;

/**
 * Implementation của ProductService
 * Xử lý toàn bộ logic nghiệp vụ liên quan đến Product
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDAO productDAO;

    @Override
    public List<Products> getAllProducts() {
        return productDAO.findAll();
    }

    @Override
    public List<Products> getProductsByCategory(Integer categoryId) {
        // Sử dụng custom query từ ProductDAO
        return productDAO.findByCategory(categoryId);
    }

    @Override
    public Products getProductById(Integer productId) {
        if (!productDAO.existsById(productId)) {
            return null;
        }
        return productDAO.findById(productId).get();
    }

    @Override
    public Products createProduct(Products product) throws Exception {
        // Kiểm tra productId đã tồn tại chưa
        if (productDAO.existsById(product.getProductID())) {
            throw new Exception("Product ID đã tồn tại trong hệ thống");
        }
        
        // Lưu product vào database
        return productDAO.save(product);
    }

    @Override
    public Products updateProduct(Integer productId, Products product) throws Exception {
        // Kiểm tra product có tồn tại không
        if (!productDAO.existsById(productId)) {
            throw new Exception("Product không tồn tại");
        }
        
        // Cập nhật product
        return productDAO.save(product);
    }

    @Override
    public void deleteProduct(Integer productId) throws Exception {
        // Kiểm tra product có tồn tại không
        if (!productDAO.existsById(productId)) {
            throw new Exception("Product không tồn tại");
        }
        
        // Xóa product
        productDAO.deleteById(productId);
    }

    @Override
    public boolean productExists(Integer productId) {
        return productDAO.existsById(productId);
    }
}
