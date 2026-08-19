package com.kurz.validation;

public class Order {

    // TODO-00: Add @NotBlank (with a message) to both name and street below,
    // so a blank or missing value fails validation instead of silently passing through.
    private String name;
    private String street;

    // TODO-01: Add @CreditCardNumber (with a message) to ccNumber.
    // It runs a Luhn check, catching typos and malformed numbers.
    private String ccNumber;

    // TODO-02: Add @Pattern (with a message) to ccExpiration, requiring the MM/YY shape.
    private String ccExpiration;

    // TODO-03: Add @Digits(integer = 3, fraction = 0) (with a message) to ccCVV.
    private String ccCVV;

    public Order() {
    }

    public Order(String name, String street, String ccNumber, String ccExpiration, String ccCVV) {
        this.name = name;
        this.street = street;
        this.ccNumber = ccNumber;
        this.ccExpiration = ccExpiration;
        this.ccCVV = ccCVV;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCcNumber() {
        return ccNumber;
    }

    public void setCcNumber(String ccNumber) {
        this.ccNumber = ccNumber;
    }

    public String getCcExpiration() {
        return ccExpiration;
    }

    public void setCcExpiration(String ccExpiration) {
        this.ccExpiration = ccExpiration;
    }

    public String getCcCVV() {
        return ccCVV;
    }

    public void setCcCVV(String ccCVV) {
        this.ccCVV = ccCVV;
    }
}
