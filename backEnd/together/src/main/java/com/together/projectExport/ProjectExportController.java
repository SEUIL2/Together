package com.together.projectExport;

import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*; // ✅ @RequestBody, @PostMapping 추가

import java.io.IOException;
import java.util.List; // ✅ List, Map 추가
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProjectExportController {

    private final NewPdfExportService newExportService;

    // ✅ 수정 시작: GET -> POST로 변경하고 @RequestBody 추가
    @PostMapping("/export/pdf")
    public void exportNewProjectPdf(
            @RequestParam Long projectId,
            @RequestBody Map<String, List<String>> selectedItems,
            HttpServletResponse response) {
        try {
            newExportService.exportPdf(projectId, selectedItems, response);
        } catch (IOException | DocumentException e) {
            // 간단한 예외 처리
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
    // ✅ 수정 끝
}

