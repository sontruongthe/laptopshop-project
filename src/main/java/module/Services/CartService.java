package module.Services;

import java.util.List;
import module.Domain.CartItem;
import module.Domain.CartItemDetail;

/**
 * Service xử lý nghiệp vụ liên quan đến Giỏ hàng
 * Quản lý CartItem và CartItemDetail
 */
public interface CartService {
    
    // ========== CartItem Operations ==========
    List<CartItem> getAllCartItems();
    CartItem getCartItemById(Integer cartId);
    CartItem getCartItemByUsername(String username);
    CartItem createCartItem(CartItem cartItem) throws Exception;
    CartItem updateCartItem(Integer cartId, CartItem cartItem) throws Exception;
    void deleteCartItem(Integer cartId) throws Exception;
    boolean cartItemExists(Integer cartId);
    
    // ========== CartItemDetail Operations ==========
    List<CartItemDetail> getAllCartItemDetails();
    List<CartItemDetail> getCartItemDetailsByCartId(Integer cartId);
    CartItemDetail getCartItemDetailByIds(Integer cartId, Integer cartDetailId);
    CartItemDetail createCartItemDetail(CartItemDetail cartItemDetail) throws Exception;
    CartItemDetail updateCartItemDetail(Integer cartDetailId, CartItemDetail cartItemDetail) throws Exception;
    void deleteCartItemDetail(Integer cartDetailId) throws Exception;
    boolean cartItemDetailExists(Integer cartDetailId);
}
