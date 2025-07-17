package com.e2x.klarnact.mapper;

import com.e2x.klarnact.klarna.model.Address;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class AddressMapper {
    private AddressMapper() {
    }

    public static Address map(com.commercetools.api.models.common.Address address) {
        if (address == null) return null;
        return Address.builder()
                .email(address.getEmail())
                .country(address.getCountry())
                .city(address.getCity())
                .familyName(address.getLastName())
                .givenName(address.getFirstName())
                .postalCode(address.getPostalCode())
                .streetAddress(address.getStreetName())
                .streetAddress2(address.getAdditionalStreetInfo())
                .region(isBlank(address.getRegion()) ? address.getState() : address.getRegion())
                .phone(address.getPhone())
                .build();
    }

    public static com.commercetools.api.models.common.Address map(Address address) {
        if (address == null) return null;
        return com.commercetools.api.models.common.Address.builder()
                .email(address.getEmail())
                .country(address.getCountry())
                .city(address.getCity())
                .lastName(address.getFamilyName())
                .firstName(address.getGivenName())
                .postalCode(address.getPostalCode())
                .streetName(address.getStreetAddress())
                .additionalStreetInfo(address.getStreetAddress2())
                .region(address.getRegion())
                .phone(address.getPhone())
                .build();
    }
}
