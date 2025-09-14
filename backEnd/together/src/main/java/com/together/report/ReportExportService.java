// backEnd/together/src/main/java/com/together/report/ReportExportService.java
package com.together.report;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * 보고서 PDF 추출 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportExportService {

    private final ReportRepository reportRepository;

    public ByteArrayInputStream exportReportToPdf(Long reportId) throws IOException {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 보고서를 찾을 수 없습니다. ID: " + reportId));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        String fontPath = "C:/Windows/Fonts/malgun.ttf";
        PdfFont koreanFont = PdfFontFactory.createFont(fontPath);

        // 1. 메인 타이틀
        Text mainTitle = new Text("주간 업무 보고서").setFont(koreanFont).setFontSize(24).setBold();
        document.add(new Paragraph(mainTitle).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

        // 2. 보고서 정보
        ProjectEntity project = report.getProject();

        // 2-1. project.getStudents()를 사용하여 모든 학생 목록을 가져옵니다.
        String allStudentMembers = project.getStudents().stream()
                // 이제 팀장을 제외하는 필터가 없습니다.
                .map(UserEntity::getUserName)
                .collect(Collectors.joining(", "));

        // 2-2. PDF에 메타데이터를 추가합니다. '작성자'에는 팀장을 포함한 모든 학생 팀원이 표시됩니다.
        addMetadata(document, "프로젝트 명", project.getTitle(), koreanFont);
        addMetadata(document, "보고 기간", report.getPeriod(), koreanFont);
        addMetadata(document, "작성자", allStudentMembers, koreanFont);

        // 3. 구분선
        document.add(new LineSeparator(new SolidLine(1f)).setMarginTop(15).setMarginBottom(15));

        // 4. 보고서 내용
        addSection(document, "금주 진행 내용", report.getWeeklyProgress(), koreanFont);
        addSection(document, "문제점 및 해결방안", report.getProblemsAndSolutions(), koreanFont);
        addSection(document, "향후 계획", report.getFuturePlans(), koreanFont);

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    // 헬퍼 메서드 (이하 동일)
    private void addMetadata(Document document, String label, String value, PdfFont font) {
        if (value != null && !value.isEmpty()) {
            Paragraph p = new Paragraph().setFont(font).setFontSize(11).setMarginBottom(5);
            p.add(new Text(label + " : ").setBold());
            p.add(new Text(value));
            document.add(p);
        }
    }

    private void addSection(Document document, String title, String content, PdfFont font) {
        if (content != null && !content.isEmpty()) {
            Text sectionTitle = new Text(title).setFont(font).setFontSize(14).setBold().setFontColor(ColorConstants.DARK_GRAY);
            Paragraph contentParagraph = new Paragraph(content).setFont(font).setFontSize(11).setMarginTop(5).setMarginLeft(10);

            document.add(new Paragraph(sectionTitle).setMarginTop(20).setMarginBottom(10));
            document.add(contentParagraph);
        }
    }
}