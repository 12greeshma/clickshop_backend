package com.trivium.dao;

import com.trivium.entity.Cart;
import com.trivium.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartDAOImpl implements CartDAO {

    @Autowired
    private CartRepository cartRepository;

    @Override
    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public List<Cart> findByCustomerId(int customerId) {
        return cartRepository.findByCustomerId(customerId);
    }

    @Override
    public void deleteByCustomerId(int customerId) {
        cartRepository.deleteByCustomerId(customerId);
    }
}
