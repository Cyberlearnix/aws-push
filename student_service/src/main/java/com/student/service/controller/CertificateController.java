package com.student.service.controller;

import com.student.service.dto.CertificateResponse;
import com.student.service.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students/{id}")
@RequiredArgsConstructor
@Slf4j
public class CertificateController {
    
    private final CertificateService certificateService;
    
    @GetMapping("/certificates")
    public ResponseEntity<List<CertificateResponse>> getCertificates(@PathVariable Long id) {
        log.info("Fetching certificates for student {}", id);
        
        List<CertificateResponse> response = certificateService.getCertificates(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/certificates/{certificateId}")
    public ResponseEntity<CertificateResponse> getCertificate(
            @PathVariable Long id,
            @PathVariable Long certificateId) {
        
        log.info("Fetching certificate {} for student {}", certificateId, id);
        
        CertificateResponse response = certificateService.getCertificate(id, certificateId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/certificates/verify/{verificationCode}")
    public ResponseEntity<CertificateResponse> verifyCertificate(
            @PathVariable String verificationCode) {
        
        log.info("Verifying certificate with code {}", verificationCode);
        
        CertificateResponse response = certificateService.verifyCertificate(verificationCode);
        return ResponseEntity.ok(response);
    }
}








