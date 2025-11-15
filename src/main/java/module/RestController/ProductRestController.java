package module.RestController;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import module.Domain.Products;
import module.Services.ProductService;

@CrossOrigin("*")
@RestController
@RequestMapping("restProduct")
public class ProductRestController {
	
	@Autowired
	private ProductService productService;

	@GetMapping("/products")
	public ResponseEntity<List<Products>> getAll() {
		return ResponseEntity.ok(productService.getAllProducts());
	}

	@GetMapping("/category/{categoryid}")
	public ResponseEntity<List<Products>> getproductcate(@PathVariable("categoryid") Integer categoryid) {
		return ResponseEntity.ok(productService.getProductsByCategory(categoryid));
	}

	@GetMapping("/products/{productID}")
	public ResponseEntity<Products> getOne(@PathVariable("productID") Integer productID) {
		Products product = productService.getProductById(productID);
		if (product == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(product);
	}

	@PostMapping("/products")
	public ResponseEntity<Products> Post(@RequestBody Products product) {
		try {
			Products created = productService.createProduct(product);
			return ResponseEntity.ok(created);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping("/products/{productID}")
	public ResponseEntity<Products> Put(@PathVariable("productID") Integer productID, @RequestBody Products product) {
		try {
			Products updated = productService.updateProduct(productID, product);
			return ResponseEntity.ok(updated);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/products/{productID}")
	public ResponseEntity<Void> Delete(@PathVariable("productID") Integer productID) {
		try {
			productService.deleteProduct(productID);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
}
