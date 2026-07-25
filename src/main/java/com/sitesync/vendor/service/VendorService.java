package com.sitesync.vendor.service;

import com.sitesync.auth.entity.User;
import com.sitesync.auth.entity.UserRole;
import com.sitesync.auth.repository.UserRepository;
import com.sitesync.vendor.dto.VendorRequests.CreateVendorRequest;
import com.sitesync.vendor.dto.VendorRequests.UpdateVendorRequest;
import com.sitesync.vendor.dto.VendorResponse;
import com.sitesync.vendor.entity.Vendor;
import com.sitesync.vendor.repository.VendorRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Each public method corresponds to a VendorRepository method on the
 * Flutter side - see vendor_repository.dart's doc comments for the exact
 * contract.
 */
@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    public VendorService(VendorRepository vendorRepository, UserRepository userRepository) {
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
    }

    /**
     * getVendors() on the Flutter side takes no projectId - it's called
     * by both vendor_config_screen.dart (contractor managing their own
     * directory) AND material_log_screen.dart (supervisor picking a
     * vendor to log a delivery against). Since a supervisor has no "own"
     * vendors, the scoping rule is: a contractor sees vendors where
     * contractor_id = their own id; a supervisor sees vendors belonging
     * to whichever contractor invited them (invited_by_contractor_id -
     * see migration_001_supervisor_invite.sql / AuthService.inviteSupervisor).
     */
    public List<VendorResponse> getVendorsForRequester(UUID requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        UUID ownerContractorId = requester.getRole() == UserRole.SUPERVISOR
                ? requester.getInvitedByContractorId()
                : requesterId;

        if (ownerContractorId == null) {
            return List.of();
        }

        return vendorRepository.findByContractorIdOrderByCreatedAtDesc(ownerContractorId).stream()
                .map(VendorResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public VendorResponse createVendor(UUID contractorId, CreateVendorRequest request) {
        Vendor vendor = new Vendor();
        vendor.setName(request.name);
        vendor.setPhoneNumber(request.phoneNumber);
        vendor.setMaterialTags(request.materialTags != null ? request.materialTags : List.of());
        vendor.setContractorId(contractorId);
        vendor.setNotes(request.notes);

        Vendor saved = vendorRepository.save(vendor);
        return VendorResponse.fromEntity(saved);
    }

    /**
     * Only the owning contractor may update their own vendor - a
     * supervisor can READ vendors (via getVendorsForRequester) but never
     * mutate the directory, matching how vendor_config_screen.dart (the
     * only screen that calls update/delete) is contractor-only.
     */
    public VendorResponse updateVendor(UUID vendorId, UUID requesterId, UpdateVendorRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found."));

        if (!vendor.getContractorId().equals(requesterId)) {
            throw new AccessDeniedException("You can only update your own vendors.");
        }

        vendor.setName(request.name);
        vendor.setPhoneNumber(request.phoneNumber);
        vendor.setMaterialTags(request.materialTags != null ? request.materialTags : List.of());
        vendor.setNotes(request.notes);

        Vendor saved = vendorRepository.save(vendor);
        return VendorResponse.fromEntity(saved);
    }

    public void deleteVendor(UUID vendorId, UUID requesterId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found."));

        if (!vendor.getContractorId().equals(requesterId)) {
            throw new AccessDeniedException("You can only delete your own vendors.");
        }

        vendorRepository.delete(vendor);
    }
}