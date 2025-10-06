package com.together.projectExport;

import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.together.Invitation.dto.TeamMemberDto;
import com.together.ProjectDetail.common.FileMetaDto;
import com.together.ProjectDetail.design.DesignDetailService;
import com.together.ProjectDetail.design.dto.DesignAllResponseDto;
import com.together.ProjectDetail.design.dto.DesignItemDto;
import com.together.ProjectDetail.develop.DevOrderItemService;
import com.together.ProjectDetail.develop.DevelopDetailService;
import com.together.ProjectDetail.develop.DevelopmentEnvironmentService;
import com.together.ProjectDetail.develop.dto.DevOrderItemResponseDto;
import com.together.ProjectDetail.develop.dto.DevelopAllResponseDto;
import com.together.ProjectDetail.develop.dto.DevelopItemDto;
import com.together.ProjectDetail.develop.dto.DevelopmentEnvironmentResponseDto;
import com.together.ProjectDetail.planning.PlanningDetailService;
import com.together.ProjectDetail.planning.dto.PlanningAllResponseDto;
import com.together.ProjectDetail.planning.dto.PlanningItemDto;
import com.together.ProjectDetail.test.IntegrationTestRowService;
import com.together.ProjectDetail.test.UnitTestRowService;
import com.together.ProjectDetail.test.dto.IntegrationTestRowResponseDto;
import com.together.ProjectDetail.test.dto.UnitTestRowResponseDto;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewPdfExportService {

    private final ProjectService projectService;
    private final PlanningDetailService planningDetailService;
    private final DesignDetailService designDetailService;
    private final DevelopmentEnvironmentService developmentEnvironmentService;
    private final DevOrderItemService devOrderItemService;
    private final DevelopDetailService developDetailService;
    private final UnitTestRowService unitTestRowService;
    private final IntegrationTestRowService integrationTestRowService;
    private BaseFont koreanFont;

    public void exportPdf(Long projectId, Map<String, List<String>> selectedItems, HttpServletResponse response) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ProjectResponseDto project = projectService.getProjectById(projectId);

        String originalFileName = project.getTitle() + "_산출물.pdf";
        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        PdfWriter.getInstance(document, response.getOutputStream());

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

        if (selectedItems.containsKey("설계")) {
            DesignAllResponseDto designData = designDetailService.getAllDesignDetails(projectId);
            addDesignSection(document, designData, selectedItems.get("설계"));
        }

        if (selectedItems.containsKey("개발")) {
            List<DevelopmentEnvironmentResponseDto> envData = developmentEnvironmentService.findAllByProjectId(projectId);
            List<DevOrderItemResponseDto> devOrderData = devOrderItemService.getDevOrderItems(projectId);
            DevelopAllResponseDto developDetailData = developDetailService.getAllDetails(projectId);
            addDevelopmentSection(document, envData, devOrderData, developDetailData, selectedItems.get("개발"));
        }

        if (selectedItems.containsKey("테스트")) {
            List<UnitTestRowResponseDto> unitTestData = unitTestRowService.listByProject(projectId);
            List<IntegrationTestRowResponseDto> integrationTestData = integrationTestRowService.listByProject(projectId);
            addTestSection(document, unitTestData, integrationTestData, selectedItems.get("테스트"));
        }

        document.close();
    }

    private void addTitlePage(Document document, ProjectResponseDto project, List<TeamMemberDto> members) throws DocumentException {
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
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private void addTableOfContents(Document document, Map<String, List<String>> selectedItems) throws DocumentException {
        Paragraph tocTitle = new Paragraph("목   차", new Font(koreanFont, 20, Font.BOLD));
        tocTitle.setAlignment(Element.ALIGN_CENTER);
        tocTitle.setSpacingBefore(20f); // 제목 위 여백 추가
        tocTitle.setSpacingAfter(50f);  // 제목과 첫 카테고리 사이 여백 증가
        document.add(tocTitle);

        Font categoryFont = new Font(koreanFont, 14, Font.BOLD);
        Font itemFont = new Font(koreanFont, 12);
        List<String> categories = Arrays.asList("기획", "설계", "개발", "테스트");

        for (String category : categories) {
            if (selectedItems.containsKey(category) && !selectedItems.get(category).isEmpty()) {
                Chunk categoryChunk = new Chunk((categories.indexOf(category) + 1) + ". " + category, categoryFont);
                categoryChunk.setLocalDestination(category);
                Paragraph categoryParagraph = new Paragraph(categoryChunk);
                categoryParagraph.setSpacingBefore(21f); // 카테고리 간 여백 증가
                categoryParagraph.setSpacingAfter(7f);  // 카테고리와 하위 항목 사이 여백 추가
                document.add(categoryParagraph);

                for (String item : selectedItems.get(category)) {
                    Paragraph itemParagraph = new Paragraph("- " + item, itemFont);
                    itemParagraph.setIndentationLeft(20f); // 들여쓰기 적용
                    itemParagraph.setSpacingAfter(6f);     // 하위 항목 간 여백 추가
                    document.add(itemParagraph);
                }
            }
        }
        document.newPage();
    }

    private void addPlanningSection(Document document, PlanningAllResponseDto planningData, List<String> selectedItems) throws DocumentException, IOException {
        document.add(createChapter("1. 기획"));
        if (selectedItems.contains("프로젝트 동기")) {
            document.add(createKeepTogetherSection(document, "프로젝트 동기", planningData.getMotivation()));
        }
        if (selectedItems.contains("프로젝트 목표")) {
            document.add(createKeepTogetherSection(document, "프로젝트 목표", planningData.getGoal()));
        }
        if (selectedItems.contains("요구사항 정의")) {
            document.add(createKeepTogetherSection(document, "요구사항 정의", planningData.getRequirement()));
        }
        if (selectedItems.contains("정보구조도")) {
            document.add(createLatestImageSection(document, "정보구조도", planningData.getInfostructure()));
        }
        if (selectedItems.contains("스토리보드")) {
            document.add(createKeepTogetherSection(document, "스토리보드", planningData.getStoryboard()));
        }
    }

    private void addDesignSection(Document document, DesignAllResponseDto designData, List<String> selectedItems) throws DocumentException, IOException {
        document.add(createChapter("2. 설계"));
        if (selectedItems.contains("유스케이스 다이어그램")) {
            document.add(createLatestImageSection(document, "유스케이스 다이어그램", designData.getUsecase()));
        }
        if (selectedItems.contains("클래스 다이어그램")) {
            document.add(createLatestImageSection(document, "클래스 다이어그램", designData.getClassDiagram()));
        }
        if (selectedItems.contains("시퀀스 다이어그램")) {
            document.add(createLatestImageSection(document, "시퀀스 다이어그램", designData.getSequence()));
        }
        if (selectedItems.contains("ERD")) {
            document.add(createLatestImageSection(document, "ERD", designData.getErd()));
        }
        if (selectedItems.contains("UI 디자인")) {
            document.add(createKeepTogetherSection(document, "UI 디자인", designData.getUi()));
        }
        if (selectedItems.contains("테이블 명세")) {
            document.add(createKeepTogetherSection(document, "테이블 명세", designData.getTable()));
        }
        if (selectedItems.contains("시스템 아키텍처")) {
            document.add(createKeepTogetherSection(document, "시스템 아키텍처", designData.getArchitecture()));
        }
    }

    private void addDevelopmentSection(Document document, List<DevelopmentEnvironmentResponseDto> envData, List<DevOrderItemResponseDto> devOrderData, DevelopAllResponseDto developDetailData, List<String> selectedItems) throws DocumentException, IOException {
        List<String> developmentItemOrder = Arrays.asList("개발 환경설정", "기능별 개발 순서", "커밋 메세지 규칙", "폴더 구조 및 파일 규칙");
        Optional<String> firstSelectedItem = developmentItemOrder.stream()
                .filter(selectedItems::contains)
                .findFirst();
        if (firstSelectedItem.isEmpty()) return;

        Chapter chapter = createChapter("3. 개발");
        boolean isFirstItemProcessed = false;
        for (String item : developmentItemOrder) {
            if (selectedItems.contains(item)) {
                Paragraph chapterTitle = null;
                if (!isFirstItemProcessed) {
                    chapterTitle = chapter.getTitle();
                    isFirstItemProcessed = true;
                }
                switch (item) {
                    case "개발 환경설정":
                        if (envData != null && !envData.isEmpty())
                            document.add(createDevelopmentEnvironmentTable(chapterTitle, "개발 환경설정", envData.get(0)));
                        break;
                    case "기능별 개발 순서":
                        if (devOrderData != null && !devOrderData.isEmpty())
                            document.add(createFeatureDevelopmentOrderTable(chapterTitle, "기능별 개발 순서", devOrderData));
                        break;
                    case "커밋 메세지 규칙":
                        document.add(createKeepTogetherSection(document, "커밋 메세지 규칙", developDetailData.getCommitRule()));
                        break;
                    case "폴더 구조 및 파일 규칙":
                        document.add(createKeepTogetherSection(document, "폴더 구조 및 파일 규칙", developDetailData.getFolder()));
                        break;
                }
            }
        }
    }

    private void addTestSection(Document document, List<UnitTestRowResponseDto> unitTestData, List<IntegrationTestRowResponseDto> integrationTestData, List<String> selectedItems) throws DocumentException {
        List<String> testItemOrder = Arrays.asList("단위테스트", "통합테스트");
        Optional<String> firstSelectedItem = testItemOrder.stream()
                .filter(selectedItems::contains)
                .findFirst();
        if (firstSelectedItem.isEmpty()) return;

        Chapter chapter = createChapter("4. 테스트");
        boolean isFirstItemProcessed = false;
        for (String item : testItemOrder) {
            if (selectedItems.contains(item)) {
                Paragraph chapterTitle = null;
                if (!isFirstItemProcessed) {
                    chapterTitle = chapter.getTitle();
                    isFirstItemProcessed = true;
                }
                switch (item) {
                    case "단위테스트":
                        if (unitTestData != null && !unitTestData.isEmpty())
                            document.add(createUnitTestTable(chapterTitle, "단위테스트", unitTestData));
                        break;
                    case "통합테스트":
                        if (integrationTestData != null && !integrationTestData.isEmpty())
                            document.add(createIntegrationTestTable(chapterTitle, "통합테스트", integrationTestData));
                        break;
                }
            }
        }
    }

    private Chapter createChapter(String title) {
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

    private PdfPTable createKeepTogetherSection(Document document, String itemTitle, PlanningItemDto itemData) throws BadElementException, IOException {
        return createKeepTogetherSectionLogic(document, itemTitle, itemData.getText(), itemData.getFiles());
    }

    private PdfPTable createKeepTogetherSection(Document document, String itemTitle, DesignItemDto itemData) throws BadElementException, IOException {
        return createKeepTogetherSectionLogic(document, itemTitle, itemData.getText(), itemData.getFiles());
    }

    private PdfPTable createKeepTogetherSection(Document document, String itemTitle, DevelopItemDto itemData) throws BadElementException, IOException {
        return createKeepTogetherSectionLogic(document, itemTitle, itemData.getText(), itemData.getFiles());
    }

    private PdfPTable createKeepTogetherSectionLogic(Document document, String itemTitle, String text, List<FileMetaDto> files) throws BadElementException, IOException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setKeepTogether(true);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        Font itemTitleFont = new Font(koreanFont, 14, Font.BOLD);
        Paragraph title = new Paragraph(itemTitle, itemTitleFont);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        cell.addElement(title);
        if (files != null) {
            for (FileMetaDto file : files) {
                if (isImageFile(file)) {
                    try {
                        Image image = Image.getInstance(new URL(file.getUrl()));
                        image.scaleToFit(PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin() - 50, Float.MAX_VALUE);
                        image.setAlignment(Element.ALIGN_CENTER);
                        image.setSpacingAfter(15f);
                        cell.addElement(image);
                    } catch (Exception e) {
                        cell.addElement(new Paragraph("이미지 로드 실패: " + file.getUrl(), new Font(koreanFont, 9, Font.ITALIC, Color.RED)));
                    }
                }
            }
        }
        Font textFont = new Font(koreanFont, 11);
        if (text != null && !text.trim().isEmpty()) {
            Paragraph textParagraph = new Paragraph(text, textFont);
            textParagraph.setSpacingAfter(20f);
            cell.addElement(textParagraph);
        }
        if (files != null && !files.isEmpty()) {
            Paragraph attachmentTitle = new Paragraph("첨부 파일 목록:", new Font(koreanFont, 10, Font.BOLD));
            attachmentTitle.setSpacingBefore(10f);
            cell.addElement(attachmentTitle);
            for (FileMetaDto file : files) {
                String fileName = file.getUrl().substring(file.getUrl().lastIndexOf('/') + 1);
                String fileTypeDescription = getFileTypeDescription(file);
                Anchor anchor = new Anchor(fileName + " " + fileTypeDescription, new Font(koreanFont, 9, Font.UNDERLINE, Color.BLUE));
                anchor.setReference(file.getUrl());
                Paragraph fileLinkParagraph = new Paragraph(anchor);
                fileLinkParagraph.setSpacingAfter(5f);
                cell.addElement(fileLinkParagraph);
            }
        }
        table.addCell(cell);
        return table;
    }

    private PdfPTable createLatestImageSection(Document document, String itemTitle, List<FileMetaDto> files) throws BadElementException, IOException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setKeepTogether(true);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        Font itemTitleFont = new Font(koreanFont, 14, Font.BOLD);
        Paragraph title = new Paragraph(itemTitle, itemTitleFont);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        cell.addElement(title);
        if (files != null && !files.isEmpty()) {
            Optional<FileMetaDto> latestImageFile = files.stream()
                    .filter(this::isImageFile)
                    .max(Comparator.comparing(FileMetaDto::getUploadedAt));
            if (latestImageFile.isPresent()) {
                FileMetaDto imageFile = latestImageFile.get();
                try {
                    Image image = Image.getInstance(new URL(imageFile.getUrl()));
                    image.scaleToFit(PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin() - 50, Float.MAX_VALUE);
                    image.setAlignment(Element.ALIGN_CENTER);
                    image.setSpacingAfter(15f);
                    cell.addElement(image);
                } catch (Exception e) {
                    cell.addElement(new Paragraph("이미지 로드 실패: " + imageFile.getUrl(), new Font(koreanFont, 9, Font.ITALIC, Color.RED)));
                }
            } else {
                cell.addElement(new Paragraph("표시할 이미지가 없습니다.", new Font(koreanFont, 11, Font.ITALIC)));
            }
        }
        table.addCell(cell);
        return table;
    }

    private PdfPTable createLatestImageSection(Document document, String itemTitle, PlanningItemDto itemData) throws BadElementException, IOException {
        return createLatestImageSection(document, itemTitle, itemData.getFiles());
    }

    private PdfPTable createLatestImageSection(Document document, String itemTitle, DesignItemDto itemData) throws BadElementException, IOException {
        return createLatestImageSection(document, itemTitle, itemData.getFiles());
    }

    private PdfPTable createDevelopmentEnvironmentTable(Paragraph chapterTitle, String itemTitle, DevelopmentEnvironmentResponseDto envDto) {
        PdfPTable outerTable = new PdfPTable(1);
        outerTable.setWidthPercentage(100);
        outerTable.setKeepTogether(true);
        outerTable.setSpacingBefore(0f);
        outerTable.setSpacingAfter(20f);
        PdfPCell outerCell = new PdfPCell();
        outerCell.setBorder(Rectangle.NO_BORDER);
        if (chapterTitle != null) {
            outerCell.addElement(chapterTitle);
        }
        Font itemTitleFont = new Font(koreanFont, 14, Font.BOLD);
        Paragraph title = new Paragraph(itemTitle, itemTitleFont);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        outerCell.addElement(title);
        PdfPTable contentTable = new PdfPTable(2);
        try {
            contentTable.setWidths(new float[]{1f, 2f});
        } catch (DocumentException ignored) {}
        contentTable.setWidthPercentage(100);
        Font labelFont = new Font(koreanFont, 11, Font.BOLD);
        Font valueFont = new Font(koreanFont, 11);
        float cellPadding = 8f;
        Color labelBgColor = new Color(242, 242, 242);
        addEnvTableRow(contentTable, "운영체제 (OS)", envDto.getOperatingSystem(), labelFont, valueFont, cellPadding, labelBgColor);
        addEnvTableRow(contentTable, "통합 개발 환경 (IDE)", envDto.getIde(), labelFont, valueFont, cellPadding, labelBgColor);
        addEnvTableRow(contentTable, "개발 언어", envDto.getDevLanguage(), labelFont, valueFont, cellPadding, labelBgColor);
        addEnvTableRow(contentTable, "프레임워크", envDto.getFramework(), labelFont, valueFont, cellPadding, labelBgColor);
        addEnvTableRow(contentTable, "버전 관리 시스템", envDto.getVersionControl(), labelFont, valueFont, cellPadding, labelBgColor);
        addEnvTableRow(contentTable, "데이터베이스", envDto.getDatabase(), labelFont, valueFont, cellPadding, labelBgColor);
        addEnvTableRow(contentTable, "기타", envDto.getEtc(), labelFont, valueFont, cellPadding, labelBgColor);
        outerCell.addElement(contentTable);
        outerTable.addCell(outerCell);
        return outerTable;
    }

    private void addEnvTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont, float padding, Color bgColor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(bgColor);
        labelCell.setPadding(padding);
        labelCell.setBorderColor(new Color(220, 220, 220));
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valueCell.setPadding(padding);
        valueCell.setBorderColor(new Color(220, 220, 220));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private PdfPTable createFeatureDevelopmentOrderTable(Paragraph chapterTitle, String itemTitle, List<DevOrderItemResponseDto> devOrderData) throws DocumentException {
        PdfPTable outerTable = new PdfPTable(1);
        outerTable.setWidthPercentage(100);
        outerTable.setKeepTogether(true);
        outerTable.setSpacingBefore(0f);
        outerTable.setSpacingAfter(20f);
        PdfPCell outerCell = new PdfPCell();
        outerCell.setBorder(Rectangle.NO_BORDER);
        if (chapterTitle != null) {
            outerCell.addElement(chapterTitle);
        }
        Font itemTitleFont = new Font(koreanFont, 14, Font.BOLD);
        Paragraph title = new Paragraph(itemTitle, itemTitleFont);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        outerCell.addElement(title);
        PdfPTable contentTable = new PdfPTable(6);
        contentTable.setWidthPercentage(100);
        contentTable.setWidths(new float[]{0.6f, 2.5f, 1f, 3f, 1.2f, 1f});
        Font headerFont = new Font(koreanFont, 11, Font.BOLD);
        Color headerBgColor = new Color(242, 242, 242);
        addHeaderCell(contentTable, "No", headerFont, headerBgColor);
        addHeaderCell(contentTable, "기능", headerFont, headerBgColor);
        addHeaderCell(contentTable, "중요도", headerFont, headerBgColor);
        addHeaderCell(contentTable, "기능 설명", headerFont, headerBgColor);
        addHeaderCell(contentTable, "작성자", headerFont, headerBgColor);
        addHeaderCell(contentTable, "완료 여부", headerFont, headerBgColor);
        Font dataFont = new Font(koreanFont, 10);
        int rowNum = 1;
        for (DevOrderItemResponseDto item : devOrderData) {
            addDataCell(contentTable, String.valueOf(rowNum++), dataFont, Element.ALIGN_CENTER);
            addDataCell(contentTable, item.getFeatureName(), dataFont, Element.ALIGN_LEFT);
            addDataCell(contentTable, item.getPriority(), dataFont, Element.ALIGN_CENTER);
            addDataCell(contentTable, item.getDescription(), dataFont, Element.ALIGN_LEFT);
            addDataCell(contentTable, item.getAuthorName(), dataFont, Element.ALIGN_CENTER);
            addDataCell(contentTable, item.isCompleted() ? "완료" : "미완료", dataFont, Element.ALIGN_CENTER);
        }
        outerCell.addElement(contentTable);
        outerTable.addCell(outerCell);
        return outerTable;
    }

    private PdfPTable createUnitTestTable(Paragraph chapterTitle, String itemTitle, List<UnitTestRowResponseDto> unitTestData) throws DocumentException {
        PdfPTable outerTable = new PdfPTable(1);
        outerTable.setWidthPercentage(100);
        outerTable.setKeepTogether(true);
        outerTable.setSpacingBefore(0f);
        outerTable.setSpacingAfter(10f);
        PdfPCell outerCell = new PdfPCell();
        outerCell.setBorder(Rectangle.NO_BORDER);
        if (chapterTitle != null) {
            outerCell.addElement(chapterTitle);
        }
        Font itemTitleFont = new Font(koreanFont, 14, Font.BOLD);
        Paragraph title = new Paragraph(itemTitle, itemTitleFont);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        outerCell.addElement(title);
        Font labelFont = new Font(koreanFont, 10, Font.BOLD);
        Font valueFont = new Font(koreanFont, 10);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (UnitTestRowResponseDto item : unitTestData) {
            PdfPTable itemTable = new PdfPTable(2);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{1f, 3f});
            itemTable.setSpacingAfter(20f);
            addUnitTestTableRow(itemTable, "테스트 ID", item.getTestId(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "메서드명", item.getMethodName(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "케이스 설명", item.getCaseDesc(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "입력 / 조건", item.getInputs(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "기대 결과", item.getExpectedResult(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "실제 결과", item.getActualResult(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "유형", item.getCaseType(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "연결된 통합 ID", item.getLinkedIntegrationId(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "완료 여부", item.isCompleted() ? "완료" : "미완료", labelFont, valueFont);
            addUnitTestTableRow(itemTable, "작성자", item.getAuthorName(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "작성일", item.getCreatedAt() != null ? item.getCreatedAt().format(formatter) : "", labelFont, valueFont);
            addUnitTestTableRow(itemTable, "수정일", item.getUpdatedAt() != null ? item.getUpdatedAt().format(formatter) : "", labelFont, valueFont);
            outerCell.addElement(itemTable);
        }
        outerTable.addCell(outerCell);
        return outerTable;
    }

    private PdfPTable createIntegrationTestTable(Paragraph chapterTitle, String itemTitle, List<IntegrationTestRowResponseDto> integrationTestData) throws DocumentException {
        PdfPTable outerTable = new PdfPTable(1);
        outerTable.setWidthPercentage(100);
        outerTable.setKeepTogether(true);
        outerTable.setSpacingBefore(0f);
        outerTable.setSpacingAfter(10f);
        PdfPCell outerCell = new PdfPCell();
        outerCell.setBorder(Rectangle.NO_BORDER);
        if (chapterTitle != null) {
            outerCell.addElement(chapterTitle);
        }
        Font itemTitleFont = new Font(koreanFont, 14, Font.BOLD);
        Paragraph title = new Paragraph(itemTitle, itemTitleFont);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(10f);
        outerCell.addElement(title);
        Font labelFont = new Font(koreanFont, 10, Font.BOLD);
        Font valueFont = new Font(koreanFont, 10);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (IntegrationTestRowResponseDto item : integrationTestData) {
            PdfPTable itemTable = new PdfPTable(2);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{1f, 3f});
            itemTable.setSpacingAfter(20f);

            // ✅ [수정] DTO의 실제 getter 메소드 이름으로 변경 (예: getExpectedResult -> getExpected)
            addUnitTestTableRow(itemTable, "테스트 ID", item.getTestId(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "모듈명", item.getModuleName(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "시나리오", item.getScenario(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "기대 결과", item.getExpected(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "실제 결과", item.getResult(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "비고", item.getNote(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "완료 여부", item.isCompleted() ? "완료" : "미완료", labelFont, valueFont);
            addUnitTestTableRow(itemTable, "작성자", item.getAuthorName(), labelFont, valueFont);
            addUnitTestTableRow(itemTable, "작성일", item.getCreatedAt() != null ? item.getCreatedAt().format(formatter) : "", labelFont, valueFont);
            addUnitTestTableRow(itemTable, "수정일", item.getUpdatedAt() != null ? item.getUpdatedAt().format(formatter) : "", labelFont, valueFont);

            outerCell.addElement(itemTable);
        }
        outerTable.addCell(outerCell);
        return outerTable;
    }

    private void addUnitTestTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(new Color(242, 242, 242));
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setPadding(8f);
        labelCell.setBorderColor(new Color(220, 220, 220));
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setPadding(8f);
        valueCell.setPaddingLeft(10f);
        valueCell.setBorderColor(new Color(220, 220, 220));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8f);
        cell.setBorderColor(new Color(220, 220, 220));
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(7f);
        cell.setBorderColor(new Color(220, 220, 220));
        table.addCell(cell);
    }

    private boolean isImageFile(FileMetaDto file) {
        String fileType = file.getFileType();
        if (fileType != null && fileType.toLowerCase().startsWith("image/")) {
            return true;
        }
        String url = file.getUrl().toLowerCase();
        return url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".gif") || url.endsWith(".bmp");
    }

    private String getFileTypeDescription(FileMetaDto file) {
        if (isImageFile(file)) return "(이미지 파일)";
        String url = file.getUrl().toLowerCase();
        if (url.endsWith(".pdf")) return "(PDF 파일)";
        if (url.endsWith(".txt")) return "(텍스트 파일)";
        if (url.endsWith(".zip") || url.endsWith(".rar")) return "(압축 파일)";
        return "(문서 파일)";
    }
}