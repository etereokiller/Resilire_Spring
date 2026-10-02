package com.resilire.backend.auth;

import com.resilire.backend.auth.dto.*;
import com.resilire.backend.common.exception.DuplicateResourceException;
import com.resilire.backend.common.validation.RutUtil;
import com.resilire.backend.doctor.DoctorProfile;
import com.resilire.backend.doctor.DoctorProfileRepository;
import com.resilire.backend.patient.PatientProfile;
import com.resilire.backend.patient.PatientProfileRepository;
import com.resilire.backend.security.CustomUserDetails;
import com.resilire.backend.security.JwtService;
import com.resilire.backend.user.Role;
import com.resilire.backend.user.User;
import com.resilire.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.email.from:no-reply@resilire.com}")
    private String fromAddress;

    @Transactional
    public AuthResponse registerPatient(RegisterPatientRequest request) {
        String rut = RutUtil.normalize(request.getRut());
        if (patientProfileRepository.existsByRut(rut) || doctorProfileRepository.existsByRut(rut)) {
            throw new DuplicateResourceException("error.account.rutExists");
        }
        User user = createUser(request.getEmail(), request.getPassword(), Role.PATIENT);

        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .rut(rut)
                .firstName(request.getFirstName())
                .lastName(request.getSurname1())
                .surname1(request.getSurname1())
                .surname2(request.getSurname2())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .build();
        patientProfileRepository.save(profile);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse registerDoctor(RegisterDoctorRequest request) {
        String rut = RutUtil.normalize(request.getRut());
        if (doctorProfileRepository.existsByRut(rut) || patientProfileRepository.existsByRut(rut)) {
            throw new DuplicateResourceException("error.account.rutExists");
        }
        User user = createUser(request.getEmail(), request.getPassword(), Role.DOCTOR);

        DoctorProfile profile = DoctorProfile.builder()
                .user(user)
                .rut(rut)
                .firstName(request.getFirstName())
                .lastName(request.getSurname1())
                .surname1(request.getSurname1())
                .surname2(request.getSurname2())
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .yearsOfExperience(request.getYearsOfExperience())
                .consultationFee(request.getConsultationFee())
                .bio(request.getBio())
                .build();
        doctorProfileRepository.save(profile);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        return buildAuthResponse(user);
    }

    /** Does not disclose whether an email is registered; delivery is enabled when SMTP is configured. */
    public void requestPasswordRecovery(String email) {
        if (email == null || email.isBlank() || userRepository.findByEmail(email.trim()).isEmpty()) {
            return;
        }
        String subject = "Resilire password recovery";
        String body = "A password recovery request was received for your Resilire account. "
                + "Please contact support to securely reset your password.";
        if (!emailEnabled) {
            log.info("[password recovery email - not sent, SMTP disabled] to={}", email);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email.trim());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception exception) {
            log.error("Failed to send password recovery email to {}", email, exception);
        }
    }

    public CurrentUserResponse getCurrentUser(CustomUserDetails principal) {
        User user = principal.getUser();
        return CurrentUserResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .build();
    }

    private User createUser(String email, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("error.account.emailExists");
        }
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
