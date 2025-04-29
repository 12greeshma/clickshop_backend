package com.trivium.service;

import com.trivium.entity.Customer;
import com.trivium.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder encoder;

    public Customer register(Customer customer) {
        customer.setPassword(encoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }
}
