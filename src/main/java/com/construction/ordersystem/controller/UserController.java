package com.construction.ordersystem.controller;

import com.construction.ordersystem.dto.ChangePasswordRequestDTO;
import com.construction.ordersystem.dto.ProfileResponseDTO;
import com.construction.ordersystem.dto.UpdateProfileRequest;
import com.construction.ordersystem.entity.User;
import com.construction.ordersystem.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Profile", description = "Xem và cập nhật hồ sơ cá nhân, đổi mật khẩu")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(userService.getProfileResponseDTO(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        User user = userService.findByEmail(userDetails.getUsername());
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok().body("Password changed successfully");
    }
}