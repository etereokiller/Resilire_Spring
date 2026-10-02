package com.resilire.backend.patient;

import com.resilire.backend.common.exception.ResourceNotFoundException;
import com.resilire.backend.patient.dto.PatientProfileResponse;
import com.resilire.backend.patient.dto.PatientProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientProfileRepository patientProfileRepository;

    @Transactional(readOnly = true)
    public PatientProfileResponse getProfileByUserId(Long userId) {
        PatientProfile profile = findByUserId(userId);
        return toResponse(profile);
    }

    @Transactional
    public PatientProfileResponse updateProfile(Long userId, PatientProfileUpdateRequest request) {
        PatientProfile profile = findByUserId(userId);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getSurname1());
        profile.setSurname1(request.getSurname1());
        profile.setSurname2(request.getSurname2());
        profile.setPhone(request.getPhone());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setAddress(request.getAddress());
        return toResponse(patientProfileRepository.save(profile));
    }

    private PatientProfile findByUserId(Long userId) {
        return patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.patient.profileNotFound"));
    }

    private PatientProfileResponse toResponse(PatientProfile profile) {
        return PatientProfileResponse.builder()
                .id(profile.getId())
                .email(profile.getUser().getEmail())
                .rut(profile.getRut())
                .firstName(profile.getFirstName())
                .surname1(profile.getSurname1())
                .surname2(profile.getSurname2())
                .phone(profile.getPhone())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .address(profile.getAddress())
                .build();
    }
}
