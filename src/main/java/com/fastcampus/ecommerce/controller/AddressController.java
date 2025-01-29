package com.fastcampus.ecommerce.controller;

import com.fastcampus.ecommerce.model.UserAddressRequest;
import com.fastcampus.ecommerce.model.UserAddressResponse;
import com.fastcampus.ecommerce.model.UserInfo;
import com.fastcampus.ecommerce.service.UserAddressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("address")
@SecurityRequirement(name = "Bearer")
@RequiredArgsConstructor
public class AddressController {
    private final UserAddressService userAddressService;

    @PostMapping
    public ResponseEntity<UserAddressResponse> create(@Valid @RequestBody UserAddressRequest addressRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        UserAddressResponse response = userAddressService.create(userInfo.getUser().getUserId(), addressRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> findAddressByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        List<UserAddressResponse> addressResponses = userAddressService.findByUserId(userInfo.getUser().getUserId());
        return ResponseEntity.ok(addressResponses);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> get(@PathVariable Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        UserAddressResponse userAddressResponse = userAddressService.findById(addressId);
        return ResponseEntity.ok(userAddressResponse);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> update(
            @PathVariable Long addressId,
            @Valid @RequestBody UserAddressRequest addressRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        UserAddressResponse response = userAddressService.update(addressId, addressRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        userAddressService.delete(addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}/set-default")
    public ResponseEntity<UserAddressResponse> setDefaultAddress(@PathVariable Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        UserAddressResponse response = userAddressService.setDefaultAddress(userInfo.getUser().getUserId(), addressId);
        return ResponseEntity.ok(response);
    }
}
