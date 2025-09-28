package com.together.projectExport;

import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPageEventHelper; // ✅ 수정: 페이지 이벤트 헬퍼 import
import com.lowagie.text.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.together.Invitation.dto.TeamMemberDto;
import com.together.ProjectDetail.common.FileMetaDto;
import com.together.ProjectDetail.planning.PlanningDetailService;
import com.together.ProjectDetail.planning.dto.PlanningAllResponseDto;
import com.together.ProjectDetail.planning.dto.PlanningItemDto;
import com.together.project.ProjectDto.ProjectResponseDto;
import com.together.project.ProjectService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewPdfExportService {

    private final ProjectService projectService;
    private final PlanningDetailService planningDetailService;
    private BaseFont koreanFont;

    public void exportPdf(Long projectId, Map<String, List<String>> selectedItems, HttpServletResponse response) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ProjectResponseDto project = projectService.getProjectById(projectId);

        String originalFileName = project.getTitle() + "_산출물.pdf";
        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        // ✅ 수정: PdfWriter를 변수에 할당하여 페이지 이벤트 설정
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new KeepTogetherPageEvent(document));

        try {
            koreanFont = BaseFont.createFont("c:/windows/fonts/malgun.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (IOException | DocumentException e) {
            throw new IOException("한글 폰트를 로드할 수 없습니다. 서버 환경의 폰트 경로를 확인하세요.", e);
        }

        document.open();

        List<TeamMemberDto> members = projectService.getProjectMembers(projectId);
        addTitlePage(document, project, members);

        addTableOfContents(document, selectedItems);

        if (selectedItems.containsKey("기획")) {
            PlanningAllResponseDto planningData = planningDetailService.getAllDetails(projectId);
            addPlanningSection(document, planningData, selectedItems.get("기획"));
        }

        document.close();
    }

    private void addTitlePage(Document document, ProjectResponseDto project, List<TeamMemberDto> members) throws DocumentException {
        // ... (이전과 동일)
        document.setMargins(0, 0, 0, 0);
        Paragraph preface = new Paragraph();
        preface.setAlignment(Element.ALIGN_CENTER);
        addEmptyLine(preface, 10);
        preface.add(new Paragraph(project.getTitle(), new Font(koreanFont, 28, Font.NORMAL, Color.BLACK)));
        addEmptyLine(preface, 1);
        LineSeparator separator = new LineSeparator(1f, 80, Color.BLACK, Element.ALIGN_CENTER, -5);
        preface.add(separator);
        addEmptyLine(preface, 3);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
        preface.add(new Paragraph(dateStr, new Font(koreanFont, 14, Font.NORMAL, Color.BLACK)));
        addEmptyLine(preface, 18);
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setWidths(new float[]{1, 3});
        String professorNames = members.stream().filter(m -> "PROFESSOR".equalsIgnoreCase(m.getRole())).map(TeamMemberDto::getUserName).collect(Collectors.joining(", "));
        String leaderName = members.stream().filter(TeamMemberDto::isLeader).map(TeamMemberDto::getUserName).findFirst().orElse("지정되지 않음");
        String memberNames = members.stream().filter(m -> !m.isLeader() && !"PROFESSOR".equalsIgnoreCase(m.getRole())).map(TeamMemberDto::getUserName).collect(Collectors.joining(", "));
        addTableCell(table, "학      과", "컴퓨터소프트웨어과");
        addTableCell(table, "담당교수", professorNames);
        addTableCell(table, "팀      장", leaderName);
        addTableCell(table, "팀      원", memberNames);
        preface.add(table);
        document.add(preface);
        document.setMargins(50, 50, 50, 50);
        document.newPage();
    }

    private void addTableCell(PdfPTable table, String header, String value) {
        // ... (이전과 동일)
        Font headerFont = new Font(koreanFont, 12, Font.BOLD);
        PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerCell.setFixedHeight(35f);
        headerCell.setBackgroundColor(new Color(242, 242, 242));
        Font valueFont = new Font(koreanFont, 12);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setPaddingLeft(15f);
        table.addCell(headerCell);
        table.addCell(valueCell);
    }

    private void addEmptyLine(Paragraph paragraph, int number) {
        // ... (이전과 동일)
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private void addTableOfContents(Document document, Map<String, List<String>> selectedItems) throws DocumentException {
        // ... (이전과 동일)
        Paragraph tocTitle = new Paragraph("목   차", new Font(koreanFont, 20, Font.BOLD));
        tocTitle.setAlignment(Element.ALIGN_CENTER);
        tocTitle.setSpacingAfter(30f);
        document.add(tocTitle);

        Font categoryFont = new Font(koreanFont, 14, Font.BOLD);
        Font itemFont = new Font(koreanFont, 12);

        List<String> categories = Arrays.asList("기획", "설계", "개발", "테스트");

        for (String category : categories) {
            if (selectedItems.containsKey(category) && !selectedItems.get(category).isEmpty()) {
                Chunk categoryChunk = new Chunk((categories.indexOf(category) + 1) + ". " + category, categoryFont);
                categoryChunk.setLocalDestination(category);
                Paragraph categoryParagraph = new Paragraph(categoryChunk);
                categoryParagraph.setSpacingBefore(10f);
                document.add(categoryParagraph);

                for (String item : selectedItems.get(category)) {
                    Paragraph itemParagraph = new Paragraph("    - " + item, itemFont);
                    document.add(itemParagraph);
                }
            }
        }
        document.newPage();
    }

    private void addPlanningSection(Document document, PlanningAllResponseDto planningData, List<String> selectedItems) throws DocumentException, IOException {
        Chapter chapter = createChapter("1. 기획");

        if (selectedItems.contains("프로젝트 동기")) {
            addItemToChapter(document, chapter, "프로젝트 동기", planningData.getMotivation());
        }

        if (selectedItems.contains("프로젝트 목표")) {
            addItemToChapter(document, chapter, "프로젝트 목표", planningData.getGoal());
        }

        if (selectedItems.contains("요구사항 정의")) {
            addItemToChapter(document, chapter, "요구사항 정의", planningData.getRequirement());
        }

        if (selectedItems.contains("정보구조도")) {
            addLatestImageItemToChapter(document, chapter, "정보구조도", planningData.getInfostructure());
        }

        // "스토리보드"를 선택했다면, 해당 내용을 문서에 추가합니다.
        if (selectedItems.contains("스토리보드")) {
            addItemToChapter(document, chapter, "스토리보드", planningData.getStoryboard());
        }

        document.add(chapter);
    }

    private Chapter createChapter(String title) {
        // ... (이전과 동일)
        Font chapterFont = new Font(koreanFont, 18, Font.BOLD);
        Paragraph chapterTitle = new Paragraph(title, chapterFont);
        chapterTitle.setSpacingBefore(10f);
        chapterTitle.setSpacingAfter(10f);

        Chapter chapter = new Chapter(chapterTitle, 1);
        chapter.setNumberDepth(0);
        chapter.setTriggerNewPage(false);

        Chunk chunk = new Chunk(title, chapterFont).setLocalDestination(title.split("\\. ")[1]);
        chapter.setTitle(new Paragraph(chunk));

        return chapter;
    }

    private void addItemToChapter(Document document, Chapter chapter, String itemTitle, PlanningItemDto itemData) throws BadElementException, IOException {
        // ... (이전과 동일)
        Font itemTitleFont = new Font(koreanFont, 14, Font.BOLD);
        Paragraph title = new Paragraph(itemTitle, itemTitleFont);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        Section section = chapter.addSection(title);
        section.setNumberDepth(0);
        section.setBookmarkTitle(itemTitle); // 북마크 이름 설정

        if (itemData.getFiles() != null) {
            for (FileMetaDto file : itemData.getFiles()) {
                if (isImageFile(file)) {
                    try {
                        Image image = Image.getInstance(new URL(file.getUrl()));
                        image.scaleToFit(PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin() - 50, Float.MAX_VALUE);
                        image.setAlignment(Element.ALIGN_CENTER);
                        image.setSpacingAfter(15f);
                        section.add(image);
                    } catch (Exception e) {
                        section.add(new Paragraph("이미지 로드 실패: " + file.getUrl(), new Font(koreanFont, 9, Font.ITALIC, Color.RED)));
                    }
                }
            }
        }

        Font textFont = new Font(koreanFont, 11);
        if (itemData.getText() != null && !itemData.getText().trim().isEmpty()) {
            Paragraph textParagraph = new Paragraph(itemData.getText(), textFont);
            textParagraph.setSpacingAfter(20f);
            section.add(textParagraph);
        }

        if (itemData.getFiles() != null && !itemData.getFiles().isEmpty()) {
            Paragraph attachmentTitle = new Paragraph("첨부 파일 목록:", new Font(koreanFont, 10, Font.BOLD));
            attachmentTitle.setSpacingBefore(10f);
            section.add(attachmentTitle);

            for (FileMetaDto file : itemData.getFiles()) {
                String fileName = file.getUrl().substring(file.getUrl().lastIndexOf('/') + 1);
                String fileTypeDescription = getFileTypeDescription(file);
                Anchor anchor = new Anchor(fileName + " " + fileTypeDescription, new Font(koreanFont, 9, Font.UNDERLINE, Color.BLUE));
                anchor.setReference(file.getUrl());
                Paragraph fileLinkParagraph = new Paragraph();
                fileLinkParagraph.add(anchor);
                fileLinkParagraph.setSpacingAfter(5f);
                section.add(fileLinkParagraph);
            }
        }
    }

    private void addLatestImageItemToChapter(Document document, Chapter chapter, String itemTitle, PlanningItemDto itemData) throws BadElementException, IOException {
        // ... (이전과 동일)
        Font itemTitleFont = new Font(koreanFont, 14, Font.BOLD);
        Paragraph title = new Paragraph(itemTitle, itemTitleFont);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        Section section = chapter.addSection(title);
        section.setNumberDepth(0);
        section.setBookmarkTitle(itemTitle);

        if (itemData.getFiles() != null && !itemData.getFiles().isEmpty()) {
            Optional<FileMetaDto> latestImageFile = itemData.getFiles().stream()
                    .filter(this::isImageFile)
                    .max(Comparator.comparing(FileMetaDto::getUploadedAt));

            if (latestImageFile.isPresent()) {
                FileMetaDto imageFile = latestImageFile.get();
                try {
                    Image image = Image.getInstance(new URL(imageFile.getUrl()));
                    image.scaleToFit(PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin() - 50, Float.MAX_VALUE);
                    image.setAlignment(Element.ALIGN_CENTER);
                    image.setSpacingAfter(15f);
                    section.add(image);
                } catch (Exception e) {
                    section.add(new Paragraph("이미지 로드 실패: " + imageFile.getUrl(), new Font(koreanFont, 9, Font.ITALIC, Color.RED)));
                }
            } else {
                section.add(new Paragraph("표시할 이미지가 없습니다.", new Font(koreanFont, 11, Font.ITALIC)));
            }
        }
    }

    private boolean isImageFile(FileMetaDto file) {
        // ... (이전과 동일)
        String fileType = file.getFileType();
        if (fileType != null && fileType.toLowerCase().startsWith("image/")) {
            return true;
        }
        String url = file.getUrl().toLowerCase();
        return url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".gif") || url.endsWith(".bmp");
    }

    private String getFileTypeDescription(FileMetaDto file) {
        // ... (이전과 동일)
        if (isImageFile(file)) {
            return "(이미지 파일)";
        }
        String url = file.getUrl().toLowerCase();
        if (url.endsWith(".pdf")) {
            return "(PDF 파일)";
        } else if (url.endsWith(".txt")) {
            return "(텍스트 파일)";
        } else if (url.endsWith(".zip") || url.endsWith(".rar")) {
            return "(압축 파일)";
        } else {
            return "(문서 파일)";
        }
    }

    // ✅ 수정 시작: 페이지 이벤트를 처리하여 항목이 잘리는 것을 방지하는 클래스
    /**
     * 페이지 이벤트를 처리하여 항목(Section)이 페이지 중간에 잘리지 않도록 합니다.
     */
    private static class KeepTogetherPageEvent extends PdfPageEventHelper {
        private final Document document;

        public KeepTogetherPageEvent(Document document) {
            this.document = document;
        }

        @Override
        public void onGenericTag(PdfWriter writer, Document doc, Rectangle rect, String tag) {
            // 이 예제에서는 onGenericTag를 사용하지 않습니다.
        }

        /**
         * 새로운 Section이 시작될 때 호출됩니다.
         * 남은 공간이 부족하면 새 페이지를 시작합니다.
         */
        @Override
        public void onSection(PdfWriter writer, Document doc, float position, int number, Paragraph title) {
            // 예상 높이를 150으로 가정 (제목 + 약간의 내용)
            // 더 정확하게 계산하려면 title과 content의 예상 높이를 계산해야 합니다.
            float estimatedHeight = 150;
            if (writer.getVerticalPosition(true) < estimatedHeight + document.bottomMargin()) {
                document.newPage();
            }
        }
    }
    // ✅ 수정 끝
}
