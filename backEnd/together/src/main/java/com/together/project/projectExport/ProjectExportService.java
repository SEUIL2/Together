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

        // 한글 폰트 경로
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

            // 2. 팀원명/역할 자동 조회
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

            // 6. 표지 생성
            addTitlePage(document, font, projectName, dateStr, "컴퓨터소프트웨어과", professorNames, leader, memberNames);

            // [1] 데이터 조회
            PlanningAllResponseDto planning = planningDetailService.getAllDetails(projectId);
            DesignAllResponseDto design = designDetailService.getAllDesignDetails(projectId);
            DevelopAllResponseDto develop = developDetailService.getAllDetails(projectId);

            // [2] 섹션별 페이지 반복 생성 (불필요 항목 제거, 오른쪽 여백 반영)
            addSectionAndItemsByPage(document, font, "기획", Arrays.asList(
                    new PdfItem("프로젝트 동기", planning.getMotivation().getText(), planning.getMotivation().getFiles(), null, true),
                    new PdfItem("프로젝트 목표", planning.getGoal().getText(), planning.getGoal().getFiles(), null, false),
                    new PdfItem("요구사항 정의", planning.getRequirement().getText(), planning.getRequirement().getFiles(), null, false),
                    new PdfItem("정보구조도", planning.getInfostructure().getText(), planning.getInfostructure().getFiles(), null, false),
                    new PdfItem("스토리보드", planning.getStoryboard().getText(), planning.getStoryboard().getFiles(), null, false)
            ), tempImagePaths);

            addSectionAndItemsByPage(document, font, "설계", Arrays.asList(
                    new PdfItem("클래스 다이어그램", design.getClassDiagram().getText(), design.getClassDiagram().getFiles(), null, true),
                    new PdfItem("ERD", design.getErd().getText(), design.getErd().getFiles(), null, false),
                    new PdfItem("시퀀스 다이어그램", design.getSequence().getText(), design.getSequence().getFiles(), null, false),
                    new PdfItem("UI 설계", design.getUi().getText(), design.getUi().getFiles(), null, false),
                    new PdfItem("테이블 스키마", design.getTable().getText(), design.getTable().getFiles(), null, false),
                    new PdfItem("시스템 아키텍처", design.getArchitecture().getText(), design.getArchitecture().getFiles(), null, false)
            ), tempImagePaths);

            addSectionAndItemsByPage(document, font, "개발", Arrays.asList(
                    new PdfItem("개발 환경 설정", develop.getEnvironment().getText(), develop.getEnvironment().getFiles(), null, true),
                    new PdfItem("버전 관리 전략", develop.getVersioning().getText(), develop.getVersioning().getFiles(), null, false),
                    new PdfItem("커밋 메세지 규칙", develop.getCommitRule().getText(), develop.getCommitRule().getFiles(), null, false),
                    new PdfItem("폴더 구조 및 파일 규칙", develop.getFolder().getText(), develop.getFolder().getFiles(), null, false)
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
            String projectName,
            String dateStr,
            String dept,
            String professor,
            String leader,
            String members
    ) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        PDPageContentStream content = new PDPageContentStream(doc, page);

        float y = PDRectangle.A4.getHeight() - 170;
        float textWidth = font.getStringWidth(projectName) / 1000 * 22;
        float centerX = (PDRectangle.A4.getWidth() - textWidth) / 2;
        content.beginText();
        content.setFont(font, 22);
        content.newLineAtOffset(centerX, y);
        content.showText(projectName);
        content.endText();

        y -= 20;
        content.setStrokingColor(0, 0, 0);
        content.moveTo(80, y);
        content.lineTo(PDRectangle.A4.getWidth() - 80, y);
        content.stroke();

        y -= 60;
        String dateText = dateStr;
        float dateWidth = font.getStringWidth(dateText) / 1000 * 15;
        float dateX = (PDRectangle.A4.getWidth() - dateWidth) / 2;
        content.beginText();
        content.setFont(font, 15);
        content.newLineAtOffset(dateX, y);
        content.showText(dateText);
        content.endText();

        float boxHeight = 120;
        float rowH = 30;
        float boxTop = 120;
        float boxLeft = 110;
        float boxWidth = 340;

        content.setLineWidth(1f);
        content.addRect(boxLeft, boxTop, boxWidth, boxHeight);
        content.stroke();

        float textLeft = boxLeft + 10;
        String[][] rows = {
                {"학   과", dept},
                {"담당교수", professor},
                {"팀   장", leader},
                {"팀   원", members}
        };
        for (int i = 0; i < rows.length; i++) {
            if (i > 0) {
                float lineY = boxTop + boxHeight - (rowH * i);
                content.moveTo(boxLeft, lineY);
                content.lineTo(boxLeft + boxWidth, lineY);
                content.stroke();
            }

            float rowCenterY = boxTop + boxHeight - (rowH * i) - (rowH / 2) - 3;
            content.beginText();
            content.setFont(font, 13);
            content.newLineAtOffset(textLeft, rowCenterY);
            content.showText(rows[i][0]);
            content.endText();

            content.beginText();
            content.setFont(font, 13);
            content.newLineAtOffset(textLeft + 80, rowCenterY);
            content.showText(rows[i][1]);
            content.endText();
        }
        content.moveTo(boxLeft + 70, boxTop);
        content.lineTo(boxLeft + 70, boxTop + boxHeight);
        content.stroke();

        content.close();
    }

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
            PDPageContentStream[] contentHolder = new PDPageContentStream[]{content};

            // 섹션 제목
            contentHolder[0].beginText();
            contentHolder[0].setFont(font, 16);
            contentHolder[0].newLineAtOffset(margin, y);
            contentHolder[0].showText(sectionName);
            contentHolder[0].endText();
            y -= 30;

            // 항목 제목
            contentHolder[0].beginText();
            contentHolder[0].setFont(font, 14);
            contentHolder[0].newLineAtOffset(margin, y);
            contentHolder[0].showText(item.title);
            contentHolder[0].endText();
            y -= 25;

            // 이미지 첨부파일
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

                    if (y < drawHeight + 80) {
                        contentHolder[0].close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        contentHolder[0] = new PDPageContentStream(doc, page);
                        y = PDRectangle.A4.getHeight() - margin;
                    }
                    contentHolder[0].drawImage(img, margin, y - drawHeight, drawWidth, drawHeight);
                    y -= (drawHeight + 30);
                }
            }

            // 본문 텍스트(자동 줄바꿈)
            y = writeTextWithAutoPaging(doc, font, contentHolder, item.text, margin, y, maxWidth , fontSize);

            // 첨부파일 표기
            float linkFontSize = fontSize - 1;
            if (!imageFiles.isEmpty()) {
                y -= 5;
                for (FileMetaDto file : imageFiles) {
                    String text = "이미지파일: " + file.getUrl();
                    y = writeTextWithAutoPaging(doc, font, contentHolder, text, margin, y, maxWidth, linkFontSize);
                }
            }
            if (!nonImageFiles.isEmpty()) {
                y -= 3;
                for (FileMetaDto file : nonImageFiles) {
                    String text = "첨부파일: " + file.getUrl();
                    y = writeTextWithAutoPaging(doc, font, contentHolder, text, margin, y, maxWidth, linkFontSize);
                }
            }
            contentHolder[0].close();
        }
    }

    private float writeTextWithAutoPaging(
            PDDocument doc,
            PDType0Font font,
            PDPageContentStream[] contentHolder,
            String text, float x, float y, float maxWidth, float fontSize) throws IOException {
        if (text == null || text.isBlank()) return y;
        float leading = 1.5f * fontSize;
        float leftMargin = x;
        for (String paragraph : text.split("\n")) {
            for (String line : wrapText(font, paragraph, fontSize, maxWidth)) {
                if (y < 60) {
                    contentHolder[0].close();
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    doc.addPage(newPage);
                    contentHolder[0] = new PDPageContentStream(doc, newPage);
                    y = PDRectangle.A4.getHeight() - leftMargin;
                }
                contentHolder[0].beginText();
                contentHolder[0].setFont(font, fontSize);
                contentHolder[0].newLineAtOffset(leftMargin, y);
                contentHolder[0].showText(line);
                contentHolder[0].endText();
                y -= leading;
            }
        }
        return y;
    }

    private List<String> wrapText(PDType0Font font, String text, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < paragraph.length(); i++) {
                line.append(paragraph.charAt(i));
                float width = font.getStringWidth(line.toString()) / 1000 * fontSize;
                if (width > maxWidth) {
                    // maxWidth를 넘으면 직전까지를 한 줄로 추가, 다시 시작
                    if (line.length() > 1) {
                        lines.add(line.substring(0, line.length() - 1));
                        // 마지막 글자는 다음 줄로 넘김
                        line = new StringBuilder().append(paragraph.charAt(i));
                    } else {
                        lines.add(line.toString());
                        line = new StringBuilder();
                    }
                }
            }
            if (line.length() > 0) lines.add(line.toString());
        }
        return lines;
    }

    private boolean isImageFile(FileMetaDto file) {
        if (file.getFileType() != null && file.getFileType().toLowerCase().startsWith("image/")) {
            return true;
        }
        String url = file.getUrl().toLowerCase();
        return url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".webp");
    }

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
