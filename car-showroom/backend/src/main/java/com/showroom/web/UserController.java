package com.showroom.web;

import com.showroom.dto.Dtos.UserRequest;
import com.showroom.dto.Dtos.UserResponse;
import com.showroom.dto.Dtos.UserTypeResponse;
import com.showroom.dto.Dtos.VehicleResponse;
import com.showroom.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    static final String SHOWROOM = "X-Showroom-Id";

    private final UserService service;

    @GetMapping("/user-types")
    public List<UserTypeResponse> types() {
        return service.types();
    }

    @GetMapping("/users")
    public List<UserResponse> list(@RequestHeader(SHOWROOM) Long showroomId,
                                   @RequestParam(required = false) String type) {
        return service.list(showroomId, type);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestHeader(SHOWROOM) Long showroomId,
                               @Valid @RequestBody UserRequest request) {
        return service.create(showroomId, request);
    }

    @PutMapping("/users/{id}")
    public UserResponse update(@RequestHeader(SHOWROOM) Long showroomId, @PathVariable Long id,
                               @Valid @RequestBody UserRequest request) {
        return service.update(showroomId, id, request);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(SHOWROOM) Long showroomId, @PathVariable Long id) {
        service.delete(showroomId, id);
    }

    @GetMapping("/users/{id}/vehicles")
    public List<VehicleResponse> vehicles(@RequestHeader(SHOWROOM) Long showroomId, @PathVariable Long id) {
        return service.vehiclesOf(showroomId, id);
    }
}
