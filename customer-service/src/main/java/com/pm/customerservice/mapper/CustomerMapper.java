package com.pm.customerservice.mapper;

import com.pm.customerservice.dto.CustomerRequestDTO;
import com.pm.customerservice.dto.CustomerResponseDTO;
import com.pm.customerservice.model.Customer;

import java.time.LocalDate;

public class CustomerMapper {
    public static CustomerResponseDTO toDTO(Customer customer){

        CustomerResponseDTO customerDTO = new CustomerResponseDTO();
        customerDTO.setId(customer.getId().toString());
        customerDTO.setName(customer.getName());
        customerDTO.setAddress(customer.getAddress());
        customerDTO.setEmail(customer.getEmail());
        customerDTO.setDateOfBirth(customer.getDateOfBirth().toString());

        return customerDTO;
    }

    public static Customer toModel(CustomerRequestDTO customerRequestDTO){
        Customer customer = new Customer();
        customer.setName(customerRequestDTO.getName());
        customer.setAddress(customerRequestDTO.getAddress());
        customer.setEmail(customerRequestDTO.getEmail());
        customer.setDateOfBirth(LocalDate.parse(customerRequestDTO.getDateOfBirth()));
        customer.setRegisteredDate(LocalDate.parse(customerRequestDTO.getRegisteredDate()));
        return customer;
    }
}
