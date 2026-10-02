package com.showroom.web;

import com.showroom.dto.ShowroomRequest;
import com.showroom.dto.ShowroomResponse;
import com.showroom.service.ShowroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Public onboarding endpoint: creates a showroom with its Admin role and first admin user. */
@RestController
@RequestMapping("/api/showrooms")
@RequiredArgsConstructor
public class ShowroomController {
    private final ShowroomService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShowroomResponse create(@Valid @RequestBody ShowroomRequest request) {
        return service.create(request);
    }
}
