package com.example.resourceops.loadtest.controller;

import com.example.resourceops.loadtest.dto.LoadTestResponse;
import com.example.resourceops.loadtest.service.LoadTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@Profile("dev")
@RestController
@RequestMapping("/api/load")
@RequiredArgsConstructor
public class LoadTestController {

    private final LoadTestService loadTestService;

    /**
     * CPU 부하 테스트
     * GET /api/load/cpu?durationMs=500
     */
    @GetMapping("/cpu")
    public ResponseEntity<LoadTestResponse> cpuLoad(
            @RequestParam(defaultValue = "500") long durationMs
    ) {
        return ResponseEntity.ok(loadTestService.cpuLoad(durationMs));
    }

    /**
     * Memory 부하 테스트
     * GET /api/load/memory?sizeMb=100&holdSeconds=30
     */
    @GetMapping("/memory")
    public ResponseEntity<LoadTestResponse> memoryLoad(
            @RequestParam(defaultValue = "100") int sizeMb,
            @RequestParam(defaultValue = "30") int holdSeconds
    ) {
        return ResponseEntity.ok(loadTestService.memoryLoad(sizeMb, holdSeconds));
    }

    /**
     * 응답 지연 테스트
     * GET /api/load/delay?delayMs=1000
     */
    @GetMapping("/delay")
    public ResponseEntity<LoadTestResponse> delayLoad(
            @RequestParam(defaultValue = "1000") long delayMs
    ) {
        return ResponseEntity.ok(loadTestService.delayLoad(delayMs));
    }

    /**
     * 큰 응답 다운로드 테스트
     * GET /api/load/response?sizeMb=1
     * sizeMb: 1 ~ 50MB
     */
    @GetMapping("/response")
    public ResponseEntity<byte[]> largeResponse(
            @RequestParam(defaultValue = "1") int sizeMb
    ) {
        byte[] data = loadTestService.largeResponse(sizeMb);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(data);
    }

    /**
     * 파일 다운로드 테스트
     * GET /api/load/download?sizeMb=10
     * sizeMb: 1 ~ 50MB
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> fileDownload(
            @RequestParam(defaultValue = "10") int sizeMb
    ) {
        byte[] data = loadTestService.fileDownload(sizeMb);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test-file-" + sizeMb + "mb.bin\"")
                .contentLength(data.length)
                .body(data);
    }

    /**
     * 큰 JSON 응답 테스트
     * GET /api/load/json?count=1000
     * count: 1 ~ 10000
     */
    @GetMapping("/json")
    public ResponseEntity<List<Map<String, Object>>> largeJson(
            @RequestParam(defaultValue = "1000") int count
    ) {
        return ResponseEntity.ok(loadTestService.largeJson(count));
    }
}