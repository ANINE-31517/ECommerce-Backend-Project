package com.ecommerce.application.repository;

import com.ecommerce.application.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    boolean existsByUserIdAndCityIgnoreCaseAndStateIgnoreCaseAndCountryIgnoreCaseAndAddressLineIgnoreCaseAndZipCode(
            UUID userId,
            String city,
            String state,
            String country,
            String addressLine,
            String zipCode
    );

    List<Address> findAllByUserId(UUID id);
}
