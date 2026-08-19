package com.sitesync.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdatePhoneRequest {
    @NotBlank
    public String phoneNumber;
}