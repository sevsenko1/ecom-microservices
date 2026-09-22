package com.ecommerce.order.service;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.model.CartItem;

import com.ecommerce.order.repository.CartItemRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {


    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;
    int attempt=0;
    public List<CartItem> getCart(String userId) {

        return cartItemRepository.findByUserId(userId);
    }

    //@CircuitBreaker(name="productService",fallbackMethod = "addToCartFallBack")
    @Retry(name="retryBreaker",fallbackMethod = "addToCartFallBack")
    public boolean addToCart(String userId, CartItemRequest request) {
        System.out.println("ATTEMPT COUNT: "+ ++attempt);

// validation part
        ProductResponse productResponse =productServiceClient.getProductDetails(request.getProductId());
        if(productResponse== null || productResponse.getStockQuantity()< request.getQuantity())
            return false;

        UserResponse userResponse= userServiceClient.getUserDetails(userId);
        if(userResponse== null)
            return false;

        CartItem existingCartItem= cartItemRepository.findByUserIdAndProductId(userId,request.getProductId());
        if(existingCartItem !=null){//update the quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity()+ request.getQuantity());//quantity updated
            existingCartItem.setPrice(BigDecimal.valueOf(1000.00));//price updated
            cartItemRepository.save(existingCartItem);
        }else{// create new cartItem
            CartItem cartItem=new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(BigDecimal.valueOf(1000.00));
            cartItemRepository.save(cartItem);
        }


        return true;
    }

    public boolean addToCartFallBack(String userId,
                                     CartItemRequest request,Exception exception){
        //System.out.println("FALLBACK CALLED!");
        exception.printStackTrace();
        return false;
    }

    public boolean deleteItemFromCart(String userId, String productId) {
        // validate
      CartItem  cartItem =cartItemRepository.findByUserIdAndProductId(userId,productId);

        if(cartItem!=null){
            cartItemRepository.delete(cartItem);
        return true;}


        return false;
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);;
    }
}
