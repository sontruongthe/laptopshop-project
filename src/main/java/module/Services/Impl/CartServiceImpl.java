package module.Services.Impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import module.DAO.CartDetailDAO;
import module.DAO.CartItemDAO;
import module.Domain.CartItem;
import module.Domain.CartItemDetail;
import module.Services.CartService;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemDAO cartItemDAO;
    
    @Autowired
    private CartDetailDAO cartDetailDAO;

    // ========== CartItem Operations ==========
    
    @Override
    public List<CartItem> getAllCartItems() {
        return cartItemDAO.findAll();
    }

    @Override
    public CartItem getCartItemById(Integer cartId) {
        if (!cartItemDAO.existsById(cartId)) {
            return null;
        }
        return cartItemDAO.findById(cartId).get();
    }

    @Override
    public CartItem getCartItemByUsername(String username) {
        return cartItemDAO.findByUsername(username);
    }

    @Override
    public CartItem createCartItem(CartItem cartItem) throws Exception {
        if (cartItemDAO.existsById(cartItem.getCartID())) {
            throw new Exception("CartItem ID đã tồn tại");
        }
        return cartItemDAO.save(cartItem);
    }

    @Override
    public CartItem updateCartItem(Integer cartId, CartItem cartItem) throws Exception {
        if (!cartItemDAO.existsById(cartId)) {
            throw new Exception("CartItem không tồn tại");
        }
        cartItem.setCartID(cartId);
        return cartItemDAO.save(cartItem);
    }

    @Override
    public void deleteCartItem(Integer cartId) throws Exception {
        if (!cartItemDAO.existsById(cartId)) {
            throw new Exception("CartItem không tồn tại");
        }
        cartItemDAO.deleteById(cartId);
    }

    @Override
    public boolean cartItemExists(Integer cartId) {
        return cartItemDAO.existsById(cartId);
    }

    // ========== CartItemDetail Operations ==========
    
    @Override
    public List<CartItemDetail> getAllCartItemDetails() {
        return cartDetailDAO.findAll();
    }

    @Override
    public List<CartItemDetail> getCartItemDetailsByCartId(Integer cartId) {
        return cartDetailDAO.findByCartItem(cartId);
    }

    @Override
    public CartItemDetail getCartItemDetailByIds(Integer cartId, Integer cartDetailId) {
        return cartDetailDAO.findByCartItemAndCartItemDetail(cartId, cartDetailId);
    }

    @Override
    public CartItemDetail createCartItemDetail(CartItemDetail cartItemDetail) throws Exception {
        if (cartDetailDAO.existsById(cartItemDetail.getCartDetailID())) {
            throw new Exception("CartItemDetail ID đã tồn tại");
        }
        return cartDetailDAO.save(cartItemDetail);
    }

    @Override
    public CartItemDetail updateCartItemDetail(Integer cartDetailId, CartItemDetail cartItemDetail) throws Exception {
        if (!cartDetailDAO.existsById(cartDetailId)) {
            throw new Exception("CartItemDetail không tồn tại");
        }
        cartItemDetail.setCartDetailID(cartDetailId);
        return cartDetailDAO.save(cartItemDetail);
    }

    @Override
    public void deleteCartItemDetail(Integer cartDetailId) throws Exception {
        if (!cartDetailDAO.existsById(cartDetailId)) {
            throw new Exception("CartItemDetail không tồn tại");
        }
        cartDetailDAO.deleteById(cartDetailId);
    }

    @Override
    public boolean cartItemDetailExists(Integer cartDetailId) {
        return cartDetailDAO.existsById(cartDetailId);
    }
}
