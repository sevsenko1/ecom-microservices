package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.dto.OrderResponse;

import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
   // private final UserRepository userRepository;
    public Optional<OrderResponse> createOrder(String userId) {
        //validation for cart items ,users


        List<CartItem> cartItems=cartService.getCart(userId);
        if(cartItems.isEmpty()){
            return Optional.empty();
        }

//        Optional<User> userOptional= userRepository.findById(Long.valueOf(userId));
//        if(userOptional.isEmpty()){
//            return Optional.empty();
//        }
//
//        User user=userOptional.get(); // now we validated and accessed user

        //calculate total price

        BigDecimal totalPrice= cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO,BigDecimal::add); // now we have total price
        //create order

        Order order=new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);
        List<OrderItem> orderItems= cartItems.stream()
                .map(item -> new OrderItem(
                        null,
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        order
                ))
                .toList();
        order.setItems(orderItems); // we converted cartitems to orderItem  as list
        Order savedOrder= orderRepository.save(order);
        //clear cart

        cartService.clearCart(userId);

        rabbitTemplate.convertAndSend("order.exchange","order.tracking"
        , Map.of("orderId",savedOrder.getId(),"status","CREATED"));
        return Optional.of(mapToOrderResponse(savedOrder));




    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getItems().stream()
                        .map(orderItem -> new OrderItemDTO(
                                orderItem.getId(),
                                orderItem.getProductId(),
                                orderItem.getQuantity(),
                                orderItem.getPrice(),
                                orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()))
                        ))
                        .toList(),
                order.getCreatedAt()
                );
    }
}
