package com.fastcampus.ecommerce.model;

import com.fastcampus.ecommerce.entity.UserAddress;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class UserAddressResponse implements Serializable {
    private Long userAddressId;
    private String addressName;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public static UserAddressResponse fromUserAddress(UserAddress userAddress) {
        return UserAddressResponse.builder()
                .userAddressId(userAddress.getUserAddressId())
                .addressName(userAddress.getAddressName())
                .streetAddress(userAddress.getStreetAddress())
                .city(userAddress.getCity())
                .state(userAddress.getState())
                .postalCode(userAddress.getPostalCode())
                .country(userAddress.getCountry())
                .build();
    }
}
