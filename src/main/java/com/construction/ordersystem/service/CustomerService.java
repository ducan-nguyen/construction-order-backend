package com.construction.ordersystem.service;

import com.construction.ordersystem.entity.Customer;
import com.construction.ordersystem.entity.User;
import com.construction.ordersystem.exception.ResourceNotFoundException;
import com.construction.ordersystem.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    @Transactional
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
    
    @Transactional
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = getCustomerById(id);
        customer.setCompanyName(customerDetails.getCompanyName());
        customer.setTaxCode(customerDetails.getTaxCode());
        customer.setAddress(customerDetails.getAddress());
        customer.setPhone(customerDetails.getPhone());
        customer.setIsWholesale(customerDetails.getIsWholesale());
        return customerRepository.save(customer);
    }
    
    @Transactional
    public Customer createOrGetCustomerForUser(User user) {
        return customerRepository.findByUser(user)
            .orElseGet(() -> {
                Customer newCustomer = new Customer();
                newCustomer.setUser(user);
                newCustomer.setCompanyName(user.getFullName() + "'s Company");
                newCustomer.setPhone(user.getPhone());
                newCustomer.setIsWholesale(false);
                return customerRepository.save(newCustomer);
            });
    }
}