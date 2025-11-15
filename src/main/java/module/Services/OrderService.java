package module.Services;

import java.util.List;
import module.Domain.Order;

/**
 * Service xử lý nghiệp vụ liên quan đến Order
 * Tách logic ra khỏi Controller để dễ bảo trì và test
 */
public interface OrderService {
    
    /**
     * Lấy danh sách tất cả đơn hàng
     * @return List<Order> - Danh sách đơn hàng
     */
    List<Order> getAllOrders();
    
    /**
     * Lấy thông tin đơn hàng theo ID
     * @param orderId - ID của đơn hàng
     * @return Order - Thông tin đơn hàng hoặc null nếu không tồn tại
     */
    Order getOrderById(Integer orderId);
    
    /**
     * Lấy danh sách đơn hàng đang xử lý của user (status 1-2)
     * @param username - Email của user
     * @return List<Order> - Danh sách đơn hàng đang xử lý
     */
    List<Order> getOrderingByUsername(String username);
    
    /**
     * Lấy danh sách đơn hàng đã hoàn thành của user (status 3-4)
     * @param username - Email của user
     * @return List<Order> - Danh sách đơn hàng đã hoàn thành
     */
    List<Order> getOrderedByUsername(String username);
    
    /**
     * Tạo đơn hàng mới
     * @param order - Thông tin đơn hàng cần tạo
     * @return Order - Đơn hàng đã được tạo
     * @throws Exception - Nếu orderId đã tồn tại
     */
    Order createOrder(Order order) throws Exception;
    
    /**
     * Cập nhật thông tin đơn hàng
     * @param orderId - ID của đơn hàng cần cập nhật
     * @param order - Thông tin mới
     * @return Order - Đơn hàng đã được cập nhật
     * @throws Exception - Nếu đơn hàng không tồn tại
     */
    Order updateOrder(Integer orderId, Order order) throws Exception;
    
    /**
     * Xóa đơn hàng theo ID
     * @param orderId - ID của đơn hàng cần xóa
     * @throws Exception - Nếu đơn hàng không tồn tại
     */
    void deleteOrder(Integer orderId) throws Exception;
    
    /**
     * Kiểm tra đơn hàng có tồn tại không
     * @param orderId - ID cần kiểm tra
     * @return boolean - true nếu tồn tại, false nếu không
     */
    boolean orderExists(Integer orderId);
}
