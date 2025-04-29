package com.trivium.dto;

public class CustomerDTO {
    private int id;
    private String name;
    private String email;
    private long mobileNumber;

    public CustomerDTO(int id, String name, String email, long mobileNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public long getMobileNumber() {
        return mobileNumber;
    }
}
