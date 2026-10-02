package com.showroom.web;

import com.showroom.dto.Dtos.ShowroomRequest;
import com.showroom.dto.Dtos.ShowroomResponse;
import com.showroom.service.ShowroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/showrooms")
@RequiredArgsConstructor
public class ShowroomController {
    private final ShowroomService service;

    @GetMapping
    public List<ShowroomResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShowroomResponse create(@Valid @RequestBody ShowroomRequest request) {
        return service.create(request);
    }
}
