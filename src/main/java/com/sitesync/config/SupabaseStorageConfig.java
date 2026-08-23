package com.sitesync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Configures the AWS S3 SDK client to point at Supabase Storage's
 * S3-compatible endpoint rather than AWS.
 *
 * Key settings that make this work with Supabase:
 * - endpointOverride: Supabase's S3-compat URL instead of AWS
 * - pathStyleAccessEnabled: REQUIRED — Supabase does not support virtual-hosted
 *   bucket addressing (bucket.s3.region.amazonaws.com). Path-style puts the
 *   bucket name in the URL path instead: /bucket/key
 * - region: Supabase always reports "us-east-1" for S3 compat, regardless
 *   of where your Supabase project is actually hosted.
 */
@Configuration
public class SupabaseStorageConfig {

    @Value("${supabase.storage.endpoint}")
    private String endpoint;

    @Value("${supabase.storage.region}")
    private String region;

    @Value("${supabase.storage.access-key}")
    private String accessKey;

    @Value("${supabase.storage.secret-key}")
    private String secretKey;

    @Bean
    public S3Client supabaseS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(
                        software.amazon.awssdk.services.s3.S3Configuration.builder()
                                .pathStyleAccessEnabled(true) // REQUIRED for Supabase
                                .checksumValidationEnabled(false)
                                .build())
                .build();
    }
}