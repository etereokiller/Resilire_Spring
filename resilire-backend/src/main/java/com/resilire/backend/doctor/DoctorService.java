package com.resilire.backend.doctor;

import com.resilire.backend.common.exception.ResourceNotFoundException;
import com.resilire.backend.doctor.dto.AvailabilitySlotRequest;
import com.resilire.backend.doctor.dto.AvailabilitySlotResponse;
import com.resilire.backend.doctor.dto.DoctorProfileResponse;
import com.resilire.backend.doctor.dto.DoctorProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Duration;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Transactional(readOnly = true)
    public DoctorProfileResponse getProfileByUserId(Long userId) {
        return toResponse(findByUserId(userId));
    }

    @Transactional
    public DoctorProfileResponse updateProfile(Long userId, DoctorProfileUpdateRequest request) {
        DoctorProfile profile = findByUserId(userId);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getSurname1());
        profile.setSurname1(request.getSurname1());
        profile.setSurname2(request.getSurname2());
        profile.setPhone(request.getPhone());
        profile.setSpecialization(request.getSpecialization());
        profile.setQualification(request.getQualification());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setConsultationFee(request.getConsultationFee());
        profile.setBio(request.getBio());
        return toResponse(doctorProfileRepository.save(profile));
    }

    @Transactional
    public DoctorProfileResponse updateProfilePhoto(Long userId, MultipartFile file) {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/") || file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please upload an image up to 5 MB.");
        }
        try {
            DoctorProfile profile = findByUserId(userId);
            profile.setProfilePhoto(file.getBytes());
            profile.setProfilePhotoContentType(file.getContentType());
            return toResponse(doctorProfileRepository.save(profile));
        } catch (java.io.IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read the uploaded image.");
        }
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> listAvailability(Long userId) {
        DoctorProfile profile = findByUserId(userId);
        return doctorAvailabilityRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(profile.getId())
                .stream()
                .map(this::toAvailabilityResponse)
                .toList();
    }

    @Transactional
    public AvailabilitySlotResponse addAvailability(Long userId, AvailabilitySlotRequest request) {
        DoctorProfile profile = findByUserId(userId);
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("error.appointment.endAfterStart");
        }
        long windowMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (windowMinutes % request.getSlotDurationMinutes() != 0) {
            throw new IllegalArgumentException("error.availability.durationMustDivideWindow");
        }
        boolean overlaps = doctorAvailabilityRepository.findByDoctorIdAndDayOfWeek(profile.getId(), request.getDayOfWeek())
                .stream().anyMatch(existing -> existing.getStartTime().isBefore(request.getEndTime())
                        && request.getStartTime().isBefore(existing.getEndTime()));
        if (overlaps) {
            throw new com.resilire.backend.common.exception.DuplicateResourceException("error.availability.overlaps");
        }
        DoctorAvailability slot = DoctorAvailability.builder()
                .doctor(profile)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotDurationMinutes(request.getSlotDurationMinutes())
                .build();
        return toAvailabilityResponse(doctorAvailabilityRepository.save(slot));
    }

    @Transactional
    public void deleteAvailability(Long userId, Long availabilityId) {
        DoctorProfile profile = findByUserId(userId);
        if (!doctorAvailabilityRepository.existsByIdAndDoctorId(availabilityId, profile.getId())) {
            throw new AccessDeniedException("error.availability.forbidden");
        }
        doctorAvailabilityRepository.deleteById(availabilityId);
    }

    private DoctorProfile findByUserId(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.doctor.profileNotFound"));
    }

    private DoctorProfileResponse toResponse(DoctorProfile profile) {
        return DoctorProfileResponse.builder()
                .id(profile.getId())
                .email(profile.getUser().getEmail())
                .rut(profile.getRut())
                .firstName(profile.getFirstName())
                .surname1(profile.getSurname1())
                .surname2(profile.getSurname2())
                .phone(profile.getPhone())
                .specialization(profile.getSpecialization())
                .qualification(profile.getQualification())
                .yearsOfExperience(profile.getYearsOfExperience())
                .consultationFee(profile.getConsultationFee())
                .bio(profile.getBio())
                .hasProfilePhoto(profile.getProfilePhoto() != null)
                .build();
    }

    private AvailabilitySlotResponse toAvailabilityResponse(DoctorAvailability slot) {
        return AvailabilitySlotResponse.builder()
                .id(slot.getId())
                .dayOfWeek(slot.getDayOfWeek())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .slotDurationMinutes(slot.getSlotDurationMinutes())
                .build();
    }
}
