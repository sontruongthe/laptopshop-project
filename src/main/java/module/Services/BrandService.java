package module.Services;

import java.util.List;
import module.Domain.Brand;

/**
 * Service xử lý nghiệp vụ liên quan đến Brand (Thương hiệu)
 */
public interface BrandService {
    
    // Lấy danh sách
    List<Brand> getAllBrands();
    
    // Tìm kiếm
    Brand getBrandById(Integer brandId);
    
    // Tạo mới
    Brand createBrand(Brand brand) throws Exception;
    
    // Cập nhật
    Brand updateBrand(Integer brandId, Brand brand) throws Exception;
    
    // Xóa
    void deleteBrand(Integer brandId) throws Exception;
    
    // Kiểm tra tồn tại
    boolean brandExists(Integer brandId);
}
