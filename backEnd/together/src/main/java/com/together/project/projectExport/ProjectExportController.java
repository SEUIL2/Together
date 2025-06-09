package com.together.project.projectExport;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class ProjectExportController {

    private final ProjectExportService exportService;

    //문서추출
    @GetMapping("/export/pdf")
    public void exportProjectPdf(Long projectId, HttpServletResponse response) throws Exception {
        exportService.exportProjectPdf(projectId, response);
    }
}
