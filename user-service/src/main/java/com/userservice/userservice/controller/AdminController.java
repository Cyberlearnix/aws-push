package com.userservice.userservice.controller;

import com.userservice.userservice.dto.AdminCreateUserRequestDTO;
import com.userservice.userservice.dto.AdminUpdateUserRequestDTO;
import com.userservice.userservice.dto.UserPublicDTO;
import com.userservice.userservice.dto.UserBasicDTO;
import com.userservice.userservice.entity.UserEntity;
import com.userservice.userservice.enums.UserRole;
import com.userservice.userservice.service.UserService;
import com.userservice.userservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 1. GET /admin/all-users-details → Get all users
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all-users-details")
    public ResponseEntity<?> getAllUsers() {
        List<UserPublicDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(Map.of("success", true, "count", users.size(), "users", users));
    }

    // 1b. GET /admin/users-basic → Get all users with only id, name, email
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users-basic")
    public ResponseEntity<?> getAllUsersBasic() {
        List<UserBasicDTO> users = userService.getAllUsersBasic();
        return ResponseEntity.ok(Map.of("success", true, "count", users.size(), "users", users));
    }

    // 2. GET /admin/users/{id} → Get specific user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getPublicById(id));
    }

    // 3. POST /admin/add-users → Create user manually
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/add-users")
    public ResponseEntity<?> addUser(@RequestBody AdminCreateUserRequestDTO dto) {
        if (userService.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email already exists"));
        }
        UserEntity user = userService.registerUser(dto.getEmail(), dto.getFullName(), dto.getPhone(), dto.getPassword());
        if (dto.getRole() != null && dto.getRole() != UserRole.STUDENT) {
            user.setRole(dto.getRole());
            userService.saveUser(user); // Save the updated role
        }
        return ResponseEntity.ok(Map.of("success", true, "user", userService.getPublicById(user.getId())));
    }

    // 4. PUT /admin/users/{id} → Update user details
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody AdminUpdateUserRequestDTO dto) {
        UserEntity user = userService.getUserById(id);
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getPhoto() != null) user.setPhoto(dto.getPhoto());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getIsActive() != null) user.setIsActive(dto.getIsActive());
        // Save
        userService.registerUser(user.getEmail(), user.getFullName(), user.getPhone(), user.getPassword()); // reuse save path
        return ResponseEntity.ok(Map.of("success", true, "user", userService.getPublicById(user.getId())));
    }

    // 5. DELETE /admin/users/{id} → Delete user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        userService.adminDeleteUser(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 6. POST /admin/users/{id}/deactivate → Suspend user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable UUID id) {
        userService.setActive(id, false);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 7. POST /admin/users/{id}/activate → Reactivate user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable UUID id) {
        userService.setActive(id, true);
        return ResponseEntity.ok(Map.of("success", true));
    }
}


