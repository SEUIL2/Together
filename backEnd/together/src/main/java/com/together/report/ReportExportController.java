// backEnd/together/src/main/java/com/together/report/ReportExportController.java
package com.together.report;

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
@RequestMapping("/reports/{reportId}/export")
@RequiredArgsConstructor
public class ReportExportController {

    private final ReportExportService reportExportService;

    // 보고서 PDF 다운로드
    @GetMapping
    public ResponseEntity<InputStreamResource> exportReportToPdf(@PathVariable Long reportId) throws IOException {
        ByteArrayInputStream bis = reportExportService.exportReportToPdf(reportId);

        HttpHeaders headers = new HttpHeaders();
        // 파일 이름을 URL 인코딩하여 한글 깨짐 방지
        String encodedFileName = URLEncoder.encode("주간보고서.pdf", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        headers.add("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}