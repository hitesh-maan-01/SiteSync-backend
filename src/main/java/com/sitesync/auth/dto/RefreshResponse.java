package com.sitesync.auth.dto;

/** Matches auth_repository.dart's _RefreshPayload.fromJson: { "accessToken": string } */
public class RefreshResponse {
    public String accessToken;

    public RefreshResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}