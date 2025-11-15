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

import module.Domain.Order;
import module.Services.OrderService;

@CrossOrigin("*")
@RestController
@RequestMapping("order")
public class OrderRestController {
	
	@Autowired
	private OrderService orderService;

	@GetMapping("/Orders")
	public ResponseEntity<List<Order>> getAllOrder() {
		return ResponseEntity.ok(orderService.getAllOrders());
	}

	@GetMapping("/Order/{orderid}")
	public ResponseEntity<Order> getOne(@PathVariable("orderid") Integer orderId) {
		Order order = orderService.getOrderById(orderId);
		if (order == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(order);
	}

	@GetMapping("/Orders/{username}")
	public ResponseEntity<List<Order>> getOrdering(@PathVariable("username") String username) {
		return ResponseEntity.ok(orderService.getOrderingByUsername(username));
	}

	@GetMapping("/Ordered/{username}")
	public ResponseEntity<List<Order>> getOrdered(@PathVariable("username") String username) {
		return ResponseEntity.ok(orderService.getOrderedByUsername(username));
	}

	@PostMapping("/Order")
	public ResponseEntity<Order> Post(@RequestBody Order order) {
		try {
			Order created = orderService.createOrder(order);
			return ResponseEntity.ok(created);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping("/Order/{orderid}")
	public ResponseEntity<Order> Put(@PathVariable("orderid") Integer orderid, @RequestBody Order order) {
		try {
			Order updated = orderService.updateOrder(orderid, order);
			return ResponseEntity.ok(updated);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/Order/{orderid}")
	public ResponseEntity<Void> Delete(@PathVariable("orderid") Integer orderid) {
		try {
			orderService.deleteOrder(orderid);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
}
