package com.resilire.backend.auth.dto;

import com.resilire.backend.common.validation.ValidRut;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterPatientRequest {

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
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
}
