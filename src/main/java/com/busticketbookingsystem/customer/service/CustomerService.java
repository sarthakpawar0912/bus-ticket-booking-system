package com.busticketbookingsystem.customer.service;

import com.busticketbookingsystem.customer.dto.CustomerRequestDTO;
import com.busticketbookingsystem.customer.dto.CustomerResponseDTO;
import com.busticketbookingsystem.customer.entity.Address;
import com.busticketbookingsystem.customer.entity.Customer;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.customer.repository.AddressRepository;
import com.busticketbookingsystem.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository  customerRepository;
    private final AddressRepository addressRepository;

    public CustomerService(CustomerRepository customerRepository, AddressRepository addressRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    private CustomerResponseDTO mapToDTO(Customer customer) {
        return new CustomerResponseDTO(
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress().getCity()  // getting city from Address
        );
    }

    public CustomerResponseDTO create(CustomerRequestDTO customerRequestDTO){
        Address address=addressRepository.findById(customerRequestDTO.getAddressId())
                .orElseThrow(()-> new ResourceNotFoundException("Address not found"));

        Customer customer= Customer.builder()
                .name(customerRequestDTO.getName())
                .email(customerRequestDTO.getEmail())
                .phone(customerRequestDTO.getPhone())
                .address(address)
                .build();

        Customer saved=customerRepository.save(customer);
        return mapToDTO(saved);
    }

    public List<CustomerResponseDTO> getAll(){
        return  customerRepository.findAllWithAddress().stream().map(this::mapToDTO).toList();
    }

    public CustomerResponseDTO getById(Integer id){
        Customer customer=customerRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("customer not found"));
        return mapToDTO(customer);
    }

    public CustomerResponseDTO update(Integer id, CustomerRequestDTO dto){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer Not found"));

        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());

        return mapToDTO(customerRepository.save(customer));
    }

    public CustomerResponseDTO patch(Integer id, CustomerRequestDTO dto){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer Not found"));

        if (dto.getName() != null) {
            customer.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            customer.setEmail(dto.getEmail());
        }

        if (dto.getPhone() != null) {
            customer.setPhone(dto.getPhone());
        }

        if (dto.getAddressId() != null) {
            Address address = addressRepository.findById(dto.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
            customer.setAddress(address);
        }

        return mapToDTO(customerRepository.save(customer));
    }
    public void delete(Integer id){
        if(!customerRepository.existsById(id)){
            throw new ResourceNotFoundException("Customer not found");
        }
        customerRepository.deleteById(id); // ✅ ADD THIS
    }
}
