package com.ecommerce.application.service;

import com.ecommerce.application.CO.AddressUpdateCO;
import com.ecommerce.application.CO.UpdatePasswordCO;
import com.ecommerce.application.VO.AddressVO;
import com.ecommerce.application.entity.Address;
import com.ecommerce.application.entity.User;
import com.ecommerce.application.exception.BadRequestException;
import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.exception.UnauthorizedException;
import com.ecommerce.application.repository.AddressRepository;
import com.ecommerce.application.repository.UserRepository;
import com.ecommerce.application.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;

    @Transactional
    public void updatePassword(UpdatePasswordCO request) {
        User user = SecurityUtil.getCurrentUser();

        Optional<User> userOptional = userRepository.findById(user.getId());
        if(userOptional.isEmpty()) {
            throw new BadRequestException("User not found!");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and Confirm Password must match!");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        emailService.sendEmail(user.getEmail(), "Password has been Updated",
                "Your account password has been successfully updated!");
    }

    public void updateAddress(AddressUpdateCO request) {
        User currentUser = SecurityUtil.getCurrentUser();

        Address address = addressRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found."));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not allowed to update this address.");
        }

        if (request.getCity() != null && !request.getCity().isBlank()) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null && !request.getState().isBlank()) {
            address.setState(request.getState());
        }
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            address.setCountry(request.getCountry());
        }
        if (request.getAddressLine() != null && !request.getAddressLine().isBlank()) {
            address.setAddressLine(request.getAddressLine());
        }
        if (request.getZipCode() != null && !request.getZipCode().isBlank()) {
            address.setZipCode(request.getZipCode());
        }
        if (request.getLabel() != null && !request.getLabel().isBlank()) {
            address.setLabel(request.getLabel());
        }

        addressRepository.save(address);
    }

    public List<AddressVO> getAddresses() {
        User currentUser = SecurityUtil.getCurrentUser();

        List<Address> addresses = addressRepository.findAllByUserId(currentUser.getId());

        return addresses.stream()
                .map(this::convertToAddressVO)
                .collect(Collectors.toList());
    }

    public AddressVO convertToAddressVO(Address address) {
        return AddressVO.builder()
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .addressLine(address.getAddressLine())
                .zipCode(address.getZipCode())
                .label(address.getLabel())
                .build();
    }


}
