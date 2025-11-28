package module.Services.Impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import module.DAO.BrandDAO;
import module.Domain.Brand;
import module.Services.BrandService;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandDAO brandDAO;

    @Override
    public List<Brand> getAllBrands() {
        return brandDAO.findAll();
    }

    @Override
    public Brand getBrandById(Integer brandId) {
        if (!brandDAO.existsById(brandId)) {
            return null;
        }
        return brandDAO.findById(brandId).get();
    }

    @Override
    public Brand createBrand(Brand brand) throws Exception {
        if (brandDAO.existsById(brand.getBrandID())) {
            throw new Exception("Brand ID đã tồn tại");
        }
        return brandDAO.save(brand);
    }

    @Override
    public Brand updateBrand(Integer brandId, Brand brand) throws Exception {
        if (!brandDAO.existsById(brandId)) {
            throw new Exception("Brand không tồn tại");
        }
        brand.setBrandID(brandId);
        return brandDAO.save(brand);
    }

    @Override
    public void deleteBrand(Integer brandId) throws Exception {
        if (!brandDAO.existsById(brandId)) {
            throw new Exception("Brand không tồn tại");
        }
        brandDAO.deleteById(brandId);
    }

    @Override
    public boolean brandExists(Integer brandId) {
        return brandDAO.existsById(brandId);
    }
}
