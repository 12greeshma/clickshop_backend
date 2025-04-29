package com.trivium.service;

import com.trivium.entity.Admin;
import com.trivium.entity.Customer;
import com.trivium.repository.AdminRepository;
import com.trivium.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private AdminRepository adminRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try to find as Customer using email
        Customer customer = customerRepo.findByEmail(email).orElse(null);
        if (customer != null) {
            return new User(
                    customer.getEmail(),
                    customer.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(customer.getRole()))
            );
        }

        // Try to find as Admin using email
        Admin admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(
                admin.getEmail(),
                admin.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(normalizeRole(admin.getRole())))


        );
    }
    private String normalizeRole(String role) {
        if (role.startsWith("ROLE_")) {
            return role;
        } else {
            return "ROLE_" + role;
        }
    }

}
