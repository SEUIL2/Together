package com.together.project.projectExport;

import com.together.project.Invitation.dto.TeamMemberDto;
import com.together.project.ProjectDetail.planning.PlanningDetailService;
import com.together.project.ProjectDetail.design.DesignDetailService;
import com.together.project.ProjectDetail.develop.DevelopDetailService;
import com.together.project.ProjectDetail.planning.dto.PlanningAllResponseDto;
import com.together.project.ProjectDetail.design.dto.DesignAllResponseDto;
import com.together.project.ProjectDetail.develop.dto.DevelopAllResponseDto;
import com.together.project.ProjectDetail.common.FileMetaDto;
import com.together.project.ProjectDto.ProjectMembersDto;
import com.together.project.ProjectDto.ProjectResponseDto;
import com.together.project.ProjectService;
import lombok.RequiredArgsConstructor;
import java.util.Objects;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectExportService {

    private final PlanningDetailService planningDetailService;
    private final DesignDetailService designDetailService;
    private final DevelopDetailService developDetailService;
    private final ProjectService projectService;



    public void exportProjectPdf(Long projectId, HttpServletResponse response) throws IOException {
        PDDocument document = new PDDocument();

        // 🟢 한글 폰트 경로
        String fontPath = "C:/Windows/Fonts/malgun.ttf";
        PDType0Font font = PDType0Font.load(document, new File(fontPath));
        List<String> tempImagePaths = new ArrayList<>();
        String dateStr = java.time.LocalDate.now().toString()
                .replace("-", "년 ")
                .replaceFirst("년 (\\d{2})", "년 $1월 ")
                .replace("월 ", "월 ") + "일";

        try {
            // 1. 프로젝트명 자동 조회
            ProjectResponseDto project = projectService.getProjectById(projectId);
            String projectName = project.getTitle();

            // 2. 팀원명/역할 자동 조회 (TeamMemberDto 사용, isLeader 필드 포함)
            List<TeamMemberDto> members = projectService.getProjectMembers(projectId);

            // 3. 담당교수(들)
            String professorNames = members.stream()
                    .filter(m -> "PROFESSOR".equalsIgnoreCase(m.getRole()))
                    .map(TeamMemberDto::getUserName)
                    .collect(Collectors.joining(", "));

            // 4. 팀장(들)
            String leader = members.stream()
                    .filter(TeamMemberDto::isLeader)
                    .map(TeamMemberDto::getUserName)
                    .findFirst().orElse("");

            // 5. 팀원(팀장/교수 제외)
            String memberNames = members.stream()
                    .filter(m -> !m.isLeader() && !"PROFESSOR".equalsIgnoreCase(m.getRole()))
                    .map(TeamMemberDto::getUserName)
                    .collect(Collectors.joining(", "));

            // 6. 표지 생성 (담당교수, 팀장, 팀원 순서에 맞춰 넘기기)
            addTitlePage(document, font, projectName, dateStr, "컴퓨터소프트웨어과", professorNames, leader, memberNames);

            // 이하 기존 내용 동일...
            // [1] 데이터 조회
            PlanningAllResponseDto planning = planningDetailService.getAllDetails(projectId);
            DesignAllResponseDto design = designDetailService.getAllDesignDetails(projectId);
            DevelopAllResponseDto develop = developDetailService.getAllDetails(projectId);

            // [2] 섹션별 페이지 반복 생성
            addSectionAndItemsByPage(document, font, "기획(Planning)", Arrays.asList(
                    new PdfItem("프로젝트 동기", planning.getMotivation().getText(), planning.getMotivation().getFiles(), null, true),
                    new PdfItem("프로젝트 목표", planning.getGoal().getText(), planning.getGoal().getFiles(), null, false),
                    new PdfItem("요구사항 정의", planning.getRequirement().getText(), planning.getRequirement().getFiles(), null, false),
                    new PdfItem("정보구조도", planning.getInfostructure().getText(), planning.getInfostructure().getFiles(), null, false),
                    new PdfItem("스토리보드", planning.getStoryboard().getText(), planning.getStoryboard().getFiles(), null, false),
                    new PdfItem("프로젝트 설명", planning.getDescription().getText(), planning.getDescription().getFiles(), null, false)
            ), tempImagePaths);

            addSectionAndItemsByPage(document, font, "설계(Design)", Arrays.asList(
                    new PdfItem("클래스 다이어그램", design.getClassDiagram().getText(), design.getClassDiagram().getFiles(), null, true),
                    new PdfItem("ERD", design.getErd().getText(), design.getErd().getFiles(), null, false),
                    new PdfItem("시퀀스 다이어그램", design.getSequence().getText(), design.getSequence().getFiles(), null, false),
                    new PdfItem("UI 설계", design.getUi().getText(), design.getUi().getFiles(), null, false),
                    new PdfItem("테이블 스키마", design.getTable().getText(), design.getTable().getFiles(), null, false),
                    new PdfItem("시스템 아키텍처", design.getArchitecture().getText(), design.getArchitecture().getFiles(), null, false),
                    new PdfItem("개발 일정/계획", design.getSchedule().getText(), design.getSchedule().getFiles(), null, false)
            ), tempImagePaths);

            addSectionAndItemsByPage(document, font, "개발(Develop)", Arrays.asList(
                    new PdfItem("개발환경", develop.getEnvironment().getText(), develop.getEnvironment().getFiles(), null, true),
                    new PdfItem("버전관리 전략", develop.getVersioning().getText(), develop.getVersioning().getFiles(), null, false),
                    new PdfItem("커밋 규칙", develop.getCommitRule().getText(), develop.getCommitRule().getFiles(), null, false),
                    new PdfItem("폴더/파일 구조", develop.getFolder().getText(), develop.getFolder().getFiles(), null, false),
                    new PdfItem("코딩 표준", develop.getCodingStandard().getText(), develop.getCodingStandard().getFiles(), null, false),
                    new PdfItem("단위 테스트", develop.getUnitTest().getText(), develop.getUnitTest().getFiles(), null, false),
                    new PdfItem("통합 테스트", develop.getIntegrationTest().getText(), develop.getIntegrationTest().getFiles(), null, false)
            ), tempImagePaths);

            // [3] PDF 다운로드 응답
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=project_export.pdf");
            document.save(response.getOutputStream());
        } finally {
            document.close();
            for (String path : tempImagePaths) {
                try { new File(path).delete(); } catch (Exception ignored) {}
            }
        }
    }
    private void addTitlePage(
            PDDocument doc,
            PDType0Font font,
            String projectName,    // 프로젝트명
            String dateStr,        // "2025년 06월 08일"
            String dept,           // 학과
            String professor,      // 담당교수
            String leader,         // 팀장
            String members         // 팀원
    ) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        PDPageContentStream content = new PDPageContentStream(doc, page);

        // 1. 제목(프로젝트명) 가운데 정렬
        float y = PDRectangle.A4.getHeight() - 170;
        float textWidth = font.getStringWidth(projectName) / 1000 * 22;
        float centerX = (PDRectangle.A4.getWidth() - textWidth) / 2;
        content.beginText();
        content.setFont(font, 22);
        content.newLineAtOffset(centerX, y);
        content.showText(projectName);
        content.endText();

        // 2. 구분선
        y -= 20;
        content.setStrokingColor(0, 0, 0);
        content.moveTo(80, y);
        content.lineTo(PDRectangle.A4.getWidth() - 80, y);
        content.stroke();

        // 3. 날짜 (가운데 정렬, 표와 겹치지 않게 위에)
        y -= 60;
        String dateText = dateStr;
        float dateWidth = font.getStringWidth(dateText) / 1000 * 15;
        float dateX = (PDRectangle.A4.getWidth() - dateWidth) / 2;
        content.beginText();
        content.setFont(font, 15);
        content.newLineAtOffset(dateX, y);
        content.showText(dateText);
        content.endText();

        // 4. 표 영역 ("학과", "담당교수", "팀장", "팀원")를 페이지 하단으로 내림
        float boxHeight = 120;          // 총 4행 × 30px
        float rowH = 30;                // 행간 넓게!
        float boxTop = 120;             // 표의 하단 위치(여백 50 남김)
        float boxLeft = 110;
        float boxWidth = 340;

        content.setLineWidth(1f);
        content.addRect(boxLeft, boxTop, boxWidth, boxHeight);
        content.stroke();

        // 5. 표 내부 (텍스트를 각 칸의 "세로 중앙"에 맞춤)
        float textLeft = boxLeft + 10;
        String[][] rows = {
                {"학   과", dept},
                {"담당교수", professor},
                {"팀   장", leader},
                {"팀   원", members}
        };
        for (int i = 0; i < rows.length; i++) {
            // 1) 구분선(가로)
            if (i > 0) {
                float lineY = boxTop + boxHeight - (rowH * i);
                content.moveTo(boxLeft, lineY);
                content.lineTo(boxLeft + boxWidth, lineY);
                content.stroke();
            }

            // 2) 텍스트(행의 세로 중앙에 맞춤)
            float rowCenterY = boxTop + boxHeight - (rowH * i) - (rowH / 2) - 3; // +7은 폰트크기 보정
            // (1) 항목명
            content.beginText();
            content.setFont(font, 13);
            content.newLineAtOffset(textLeft, rowCenterY);
            content.showText(rows[i][0]);
            content.endText();

            // (2) 값
            content.beginText();
            content.setFont(font, 13);
            content.newLineAtOffset(textLeft + 80, rowCenterY);
            content.showText(rows[i][1]);
            content.endText();
        }
        // 6. 세로구분선
        content.moveTo(boxLeft + 70, boxTop);
        content.lineTo(boxLeft + 70, boxTop + boxHeight);
        content.stroke();

        content.close();
    }



    // 각 항목별 새 페이지/왼쪽여백, 오른쪽여백, 이미지/파일 분리 처리
    private void addSectionAndItemsByPage(
            PDDocument doc,
            PDType0Font font,
            String sectionName,
            List<PdfItem> items,
            List<String> tempImagePaths
    ) throws IOException {
        float margin = 50;
        float rightMargin = 50;
        float maxWidth = PDRectangle.A4.getWidth() - margin - rightMargin;
        float fontSize = 11f;

        for (PdfItem item : items) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            float y = PDRectangle.A4.getHeight() - margin;
            PDPageContentStream content = new PDPageContentStream(doc, page);

            // ★ 섹션(기획/설계/개발) 제목 출력
            content.beginText();
            content.setFont(font, 16);
            content.newLineAtOffset(margin, y);
            content.showText(sectionName);
            content.endText();
            y -= 30;

            // 항목 제목
            content.beginText();
            content.setFont(font, 14);
            content.newLineAtOffset(margin, y);
            content.showText(item.title);
            content.endText();
            y -= 25;

            // 1) 이미지 첨부파일 (본문 미리보기, 비율 유지, 간격 30)
            int maxImgWidth = (int)maxWidth;
            int maxImgHeight = 350;
            List<FileMetaDto> imageFiles = new ArrayList<>();
            List<FileMetaDto> nonImageFiles = new ArrayList<>();

            if (item.files != null && !item.files.isEmpty()) {
                for (FileMetaDto file : item.files) {
                    if (isImageFile(file)) imageFiles.add(file);
                    else nonImageFiles.add(file);
                }
            }

            for (FileMetaDto file : imageFiles) {
                String imgPath = downloadImageToTemp(file.getUrl());
                if (imgPath != null) {
                    tempImagePaths.add(imgPath);
                    PDImageXObject img = PDImageXObject.createFromFile(imgPath, doc);
                    int iw = img.getWidth();
                    int ih = img.getHeight();
                    double ratio = Math.min((double)maxImgWidth / iw, (double)maxImgHeight / ih);
                    int drawWidth = (int)(iw * ratio);
                    int drawHeight = (int)(ih * ratio);

                    if (y < drawHeight + 80) { // 새 페이지 조건
                        content.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        content = new PDPageContentStream(doc, page);
                        y = PDRectangle.A4.getHeight() - margin;
                    }
                    content.drawImage(img, margin, y - drawHeight, drawWidth, drawHeight);
                    y -= (drawHeight + 30); // 이미지 사이 충분한 간격
                }
            }

            // 2) 본문 텍스트(자동 줄바꿈)
            y = writeTextWithAutoPaging(doc, font, content, item.text, margin, y, maxWidth, fontSize);

            // 3) 첨부파일 표기 (이미지/비이미지 분리)
            float linkFontSize = fontSize - 1;
            if (!imageFiles.isEmpty()) {
                y -= 5;
                for (FileMetaDto file : imageFiles) {
                    String text = "이미지파일: " + file.getUrl();
                    y = writeTextWithAutoPaging(doc, font, content, text, margin, y, maxWidth, linkFontSize);
                }
            }
            if (!nonImageFiles.isEmpty()) {
                y -= 3;
                for (FileMetaDto file : nonImageFiles) {
                    String text = "첨부파일: " + file.getUrl();
                    y = writeTextWithAutoPaging(doc, font, content, text, margin, y, maxWidth, linkFontSize);
                }
            }
            content.close();
        }
    }

    // 줄바꿈 + 페이지 넘김 (왼쪽/오른쪽 여백)
    private float writeTextWithAutoPaging(PDDocument doc, PDType0Font font, PDPageContentStream content,
                                          String text, float x, float y, float maxWidth, float fontSize) throws IOException {
        if (text == null || text.isBlank()) return y;
        float leading = 1.5f * fontSize;
        float leftMargin = x;
        for (String paragraph : text.split("\n")) {
            for (String line : wrapText(font, paragraph, fontSize, maxWidth)) {
                if (y < 60) { // 새 페이지
                    content.close();
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    doc.addPage(newPage);
                    content = new PDPageContentStream(doc, newPage);
                    y = PDRectangle.A4.getHeight() - leftMargin;
                }
                content.beginText();
                content.setFont(font, fontSize);
                content.newLineAtOffset(leftMargin, y);
                content.showText(line);
                content.endText();
                y -= leading;
            }
        }
        return y;
    }

    // 텍스트 줄 wrap
    private List<String> wrapText(PDType0Font font, String text, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float width = font.getStringWidth(testLine) / 1000 * fontSize;
            if (width > maxWidth) {
                if (line.length() > 0) lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    // 이미지파일 여부 (fileType 우선)
    private boolean isImageFile(FileMetaDto file) {
        if (file.getFileType() != null && file.getFileType().toLowerCase().startsWith("image/")) {
            return true;
        }
        String url = file.getUrl().toLowerCase();
        return url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".webp");
    }

    // 이미지 임시 다운로드 (webp 포함)
    private String downloadImageToTemp(String fileUrl) {
        try {
            String actualUrl = fileUrl;
            if (fileUrl.contains("drive.google.com/file/d/")) {
                int start = fileUrl.indexOf("/d/") + 3;
                int end = fileUrl.indexOf("/", start);
                if (end == -1) {
                    end = fileUrl.indexOf("?", start);
                    if (end == -1) end = fileUrl.length();
                }
                String fileId = fileUrl.substring(start, end);
                actualUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
            }
            String ext = ".png";
            if (actualUrl.endsWith(".jpg") || actualUrl.endsWith(".jpeg")) ext = ".jpg";
            if (actualUrl.endsWith(".webp")) ext = ".webp";
            String tempPath = System.getProperty("java.io.tmpdir") + "/pdfimg-" + UUID.randomUUID() + ext;
            try (InputStream in = new URL(actualUrl).openStream()) {
                Files.copy(in, Paths.get(tempPath));
                return tempPath;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // PDF 출력용 내부 구조체
    private static class PdfItem {
        String title;
        String text;
        List<FileMetaDto> files;
        String mainImageUrl;
        boolean firstInSection;
        PdfItem(String title, String text, List<FileMetaDto> files, String mainImageUrl, boolean firstInSection) {
            this.title = title;
            this.text = text;
            this.files = files;
            this.mainImageUrl = mainImageUrl;
            this.firstInSection = firstInSection;
        }
    }
}
