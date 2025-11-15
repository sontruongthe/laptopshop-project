package module.Services.Impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import module.DAO.OrderDAO;
import module.Domain.Order;
import module.Services.OrderService;

/**
 * Implementation của OrderService
 * Xử lý toàn bộ logic nghiệp vụ liên quan đến Order
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDAO orderDAO;

    @Override
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    @Override
    public Order getOrderById(Integer orderId) {
        if (!orderDAO.existsById(orderId)) {
            return null;
        }
        return orderDAO.findById(orderId).get();
    }

    @Override
    public List<Order> getOrderingByUsername(String username) {
        // Lấy đơn hàng đang xử lý (status 1-2)
        return orderDAO.findOrderingByUsername(username);
    }

    @Override
    public List<Order> getOrderedByUsername(String username) {
        // Lấy đơn hàng đã hoàn thành (status 3-4)
        return orderDAO.findOrderedByUsername(username);
    }

    @Override
    public Order createOrder(Order order) throws Exception {
        // Kiểm tra orderId đã tồn tại chưa
        if (order.getOrderID() != null && orderDAO.existsById(order.getOrderID())) {
            throw new Exception("Order ID đã tồn tại trong hệ thống");
        }
        
        // Lưu order vào database
        return orderDAO.save(order);
    }

    @Override
    public Order updateOrder(Integer orderId, Order order) throws Exception {
        // Kiểm tra order có tồn tại không
        if (!orderDAO.existsById(orderId)) {
            throw new Exception("Order không tồn tại");
        }
        
        // Cập nhật order
        return orderDAO.save(order);
    }

    @Override
    public void deleteOrder(Integer orderId) throws Exception {
        // Kiểm tra order có tồn tại không
        if (!orderDAO.existsById(orderId)) {
            throw new Exception("Order không tồn tại");
        }
        
        // Xóa order
        orderDAO.deleteById(orderId);
    }

    @Override
    public boolean orderExists(Integer orderId) {
        return orderDAO.existsById(orderId);
    }
}
