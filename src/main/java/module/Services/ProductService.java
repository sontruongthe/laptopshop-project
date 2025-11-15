package module.Services;

import java.util.List;
import module.Domain.Products;

/**
 * Service xử lý nghiệp vụ liên quan đến Product
 * Tách logic ra khỏi Controller để dễ bảo trì và test
 */
public interface ProductService {
    
    /**
     * Lấy danh sách tất cả sản phẩm
     * @return List<Products> - Danh sách sản phẩm
     */
    List<Products> getAllProducts();
    
    /**
     * Lọc sản phẩm theo danh mục
     * @param categoryId - ID của category
     * @return List<Products> - Danh sách sản phẩm thuộc category
     */
    List<Products> getProductsByCategory(Integer categoryId);
    
    /**
     * Lấy thông tin sản phẩm theo ID
     * @param productId - ID của sản phẩm
     * @return Products - Thông tin sản phẩm hoặc null nếu không tồn tại
     */
    Products getProductById(Integer productId);
    
    /**
     * Tạo sản phẩm mới
     * @param product - Thông tin sản phẩm cần tạo
     * @return Products - Sản phẩm đã được tạo
     * @throws Exception - Nếu productId đã tồn tại
     */
    Products createProduct(Products product) throws Exception;
    
    /**
     * Cập nhật thông tin sản phẩm
     * @param productId - ID của sản phẩm cần cập nhật
     * @param product - Thông tin mới
     * @return Products - Sản phẩm đã được cập nhật
     * @throws Exception - Nếu sản phẩm không tồn tại
     */
    Products updateProduct(Integer productId, Products product) throws Exception;
    
    /**
     * Xóa sản phẩm theo ID
     * @param productId - ID của sản phẩm cần xóa
     * @throws Exception - Nếu sản phẩm không tồn tại
     */
    void deleteProduct(Integer productId) throws Exception;
    
    /**
     * Kiểm tra sản phẩm có tồn tại không
     * @param productId - ID cần kiểm tra
     * @return boolean - true nếu tồn tại, false nếu không
     */
    boolean productExists(Integer productId);
}
