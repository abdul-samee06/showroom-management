package com.showroom.web;

import com.showroom.dto.Dtos.PageResult;
import com.showroom.dto.Dtos.VehicleRequest;
import com.showroom.dto.Dtos.VehicleResponse;
import com.showroom.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private static final String SHOWROOM = UserController.SHOWROOM;

    private final VehicleService service;

    @GetMapping
    public PageResult<VehicleResponse> list(@RequestHeader(SHOWROOM) Long showroomId,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) Integer wheels,
                                            @RequestParam(defaultValue = "purchaseDate") String sortBy,
                                            @RequestParam(defaultValue = "desc") String dir,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return service.list(showroomId, q, wheels, sortBy, dir, page, size);
    }

    @GetMapping("/{id}")
    public VehicleResponse get(@RequestHeader(SHOWROOM) Long showroomId, @PathVariable Long id) {
        return service.get(showroomId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse create(@RequestHeader(SHOWROOM) Long showroomId,
                                  @Valid @RequestBody VehicleRequest request) {
        return service.create(showroomId, request);
    }

    @PutMapping("/{id}")
    public VehicleResponse update(@RequestHeader(SHOWROOM) Long showroomId, @PathVariable Long id,
                                  @Valid @RequestBody VehicleRequest request) {
        return service.update(showroomId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(SHOWROOM) Long showroomId, @PathVariable Long id) {
        service.delete(showroomId, id);
    }
}
