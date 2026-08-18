package com.dadscare.backend.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public UserDto me() {
        return userService.currentUser();
    }

    @PutMapping("/push-token")
    public void registerPushToken(@Valid @RequestBody RegisterPushTokenRequest request) {
        userService.registerPushToken(request);
    }
}
