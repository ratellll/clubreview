package com.example.clubreview.controller;

import com.example.clubreview.external.kakao.KakaoLocalClient;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/export/kakao/_diag")
@PermitAll // optional; requires @EnableMethodSecurity(jsr250Enabled = true)
public class AdminKakaoDiagController {


    private final KakaoLocalClient client;


    public AdminKakaoDiagController(KakaoLocalClient client) { this.client = client; }


    @GetMapping(value = "/raw", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> raw(@RequestParam String q,
                                      @RequestParam double x,
                                      @RequestParam double y,
                                      @RequestParam(defaultValue = "3000") int radius,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "15") int size) {
        var json = client.searchByKeyword(q, x, y, radius, page, size).block();
        return ResponseEntity.ok(json);
    }
}