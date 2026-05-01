package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.GuaranteeAcceptDto;
import ru.svoi.mastera.backend.dto.GuaranteeMeDto;
import ru.svoi.mastera.backend.service.GuaranteeTermsService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/guarantee")
@RequiredArgsConstructor
public class GuaranteeTermsController {

    private final GuaranteeTermsService guaranteeTermsService;

    @GetMapping("/me")
    public GuaranteeMeDto me(@RequestHeader("X-User-Id") UUID userId) {
        return guaranteeTermsService.getMe(userId);
    }

    @PostMapping("/accept")
    public GuaranteeMeDto accept(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody GuaranteeAcceptDto dto
    ) {
        return guaranteeTermsService.accept(userId, dto);
    }
}
