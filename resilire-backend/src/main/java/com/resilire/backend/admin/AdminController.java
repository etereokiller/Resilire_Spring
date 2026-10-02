package com.resilire.backend.admin;

import com.resilire.backend.admin.dto.AdminUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.listUsers(pageable));
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<AdminUserResponse> enableUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.setUserEnabled(id, true));
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<AdminUserResponse> disableUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.setUserEnabled(id, false));
    }
}
