package com.resilire.backend.auth.dto;

import com.resilire.backend.common.validation.ValidRut;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RegisterDoctorRequest {

    @NotBlank(message = "validation.email.required")
    @Email(message = "validation.email.invalid")
    private String email;

    @NotBlank(message = "validation.password.required")
    @Size(min = 8, message = "validation.password.minLength")
    private String password;

    @NotBlank(message = "validation.rut.required")
    @ValidRut
    private String rut;

    @NotBlank(message = "validation.firstName.required")
    private String firstName;

    @NotBlank(message = "validation.surname1.required")
    private String surname1;

    @NotBlank(message = "validation.surname2.required")
    private String surname2;

    private String phone;
    private String specialization;
    private String qualification;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private String bio;
}
