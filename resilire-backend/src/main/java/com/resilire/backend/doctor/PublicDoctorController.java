package com.resilire.backend.doctor;

import com.resilire.backend.doctor.dto.PublicDoctorDetailResponse;
import com.resilire.backend.doctor.dto.PublicDoctorSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;
import com.resilire.backend.doctor.dto.BookableSlotResponse;

@RestController
@RequestMapping("/api/public/doctors")
@RequiredArgsConstructor
public class PublicDoctorController {

    private final PublicDoctorService publicDoctorService;

    private final DoctorProfileRepository doctorProfileRepository;

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getDoctorPhoto(@PathVariable Long id) {
        DoctorProfile doctor = doctorProfileRepository.findByIdAndUserEnabledTrue(id).orElseThrow();
        if (doctor.getProfilePhoto() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(doctor.getProfilePhotoContentType())).body(doctor.getProfilePhoto());
    }

    @GetMapping
    public ResponseEntity<Page<PublicDoctorSummaryResponse>> search(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(publicDoctorService.search(specialization, name, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicDoctorDetailResponse> getDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(publicDoctorService.getDoctorDetail(id));
    }

    @GetMapping("/{id}/bookable-slots")
    public ResponseEntity<List<BookableSlotResponse>> getBookableSlots(
            @PathVariable Long id, @RequestParam LocalDate date) {
        return ResponseEntity.ok(publicDoctorService.getBookableSlots(id, date));
    }
}
