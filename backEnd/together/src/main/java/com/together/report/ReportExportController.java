// backEnd/together/src/main/java/com/together/report/ReportExportController.java
package com.together.report;

import com.together.util.customAnnotation.CurrentProject; // CurrentProject import 추가
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 보고서 PDF 추출 API 컨트롤러
 */
@RestController
@RequestMapping("/reports/export") // 공통 경로를 /reports/export로 변경
@RequiredArgsConstructor
public class ReportExportController {

    private final ReportExportService reportExportService;

    // 단일 보고서 다운로드
    @GetMapping("/{reportId}")
    public ResponseEntity<InputStreamResource> exportReportToPdf(@PathVariable Long reportId) throws IOException {
        ByteArrayInputStream bis = reportExportService.exportReportToPdf(reportId);

        HttpHeaders headers = new HttpHeaders();
        String encodedFileName = URLEncoder.encode("주간보고서.pdf", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        headers.add("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    // 현재 프로젝트의 모든 보고서 한번에 다운로드 (새로 추가된 API)
    @GetMapping("/all")
    public ResponseEntity<InputStreamResource> exportAllReportsToPdf(@CurrentProject Long projectId) throws IOException {
        ByteArrayInputStream bis = reportExportService.exportAllReportsToPdf(projectId);

        HttpHeaders headers = new HttpHeaders();
        String encodedFileName = URLEncoder.encode("전체_주간보고서.pdf", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        headers.add("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}