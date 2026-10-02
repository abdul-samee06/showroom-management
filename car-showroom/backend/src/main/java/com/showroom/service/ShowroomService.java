package com.showroom.service;

import com.showroom.domain.Showroom;
import com.showroom.dto.Dtos.ShowroomRequest;
import com.showroom.dto.Dtos.ShowroomResponse;
import com.showroom.repo.ShowroomRepository;
import com.showroom.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowroomService {
    private final ShowroomRepository showrooms;

    @Transactional(readOnly = true)
    public List<ShowroomResponse> list() {
        return showrooms.findByDeletedAtIsNullOrderByName().stream().map(ShowroomResponse::from).toList();
    }

    @Transactional
    public ShowroomResponse create(ShowroomRequest r) {
        Showroom s = new Showroom();
        s.setName(r.name().trim());
        s.setPhone(blankToNull(r.phone()));
        s.setEmail(blankToNull(r.email()));
        s.setAddress(blankToNull(r.address()));
        return ShowroomResponse.from(showrooms.save(s));
    }

    /** Throws 404 if the showroom id from the request header does not exist. */
    public void require(Long showroomId) {
        if (!showrooms.existsByIdAndDeletedAtIsNull(showroomId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Showroom not found.");
        }
    }

    static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
