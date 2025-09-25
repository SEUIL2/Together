package com.together.user.professor.feedback;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.user.professor.feedback.DTO.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackReadRepository feedbackReadRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FeedbackTextHistoryRepository feedbackTextHistoryRepository;
    private final FeedbackCategoryRepository feedbackCategoryRepository;

    //피드백 생성
    public FeedbackEntity createFeedback(CreateFeedbackRequest dto, Long userId) {
        ProjectEntity project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        UserEntity author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setProject(project);
        feedback.setAuthor(author);
        feedback.setPage(dto.getPage());
        feedback.setX(dto.getX());
        feedback.setY(dto.getY());
        feedback.setText(dto.getText());
        feedback.setIsRead(false); //false로 명시
        //feedback.setCategory(dto.getCategory());
        feedback.setCreatedAt(LocalDateTime.now());

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            Set<FeedbackCategoryEntity> categories = new HashSet<>(feedbackCategoryRepository.findAllById(dto.getCategoryIds()));
            feedback.setCategories(categories);
        }

        saveFeedbackTextHistory(author, dto.getText()); //피드백 내용 히스토리 저장

        return feedbackRepository.save(feedback);
    }

    //전체 피드백 조회
    public List<FeedbackSummaryDto> getFeedbacksByUser(Long userId, Long projectId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if(user.getRole() == UserEntity.UserRole.STUDENT){
            List<FeedbackEntity> feedbacks = feedbackRepository.findAllByProjectIn(user.getProject());

            return feedbacks.stream()
                    .map(feedback -> new FeedbackSummaryDto(
                            feedback.getFeedbackId(),
                            feedback.getCreatedAt(),
                            feedback.getPage(),
                            feedback.getText(),
                            feedback.getAuthor().getUserId(),
                            feedback.getIsRead(),
                            //feedback.getCategory()
                            feedback.getCategories().stream()
                                    .map(FeedbackCategoryEntity::getName)
                                    .collect(Collectors.toSet())
                    ))
                    .collect(Collectors.toList());
        } else if (user.getRole() == UserEntity.UserRole.PROFESSOR) {
            List<FeedbackEntity> feedbacks = feedbackRepository.findAllByProject_ProjectIdAndAuthor(projectId, user);

            return feedbacks.stream()
                    .map(feedback -> new FeedbackSummaryDto(
                            feedback.getFeedbackId(),
                            feedback.getCreatedAt(),
                            feedback.getPage(),
                            feedback.getText(),
                            feedback.getAuthor().getUserId(),
                            true, // 교수가 작성한 피드백은 본인이므로 항상 읽은 상태(고정값)
                            //feedback.getCategory()
                            feedback.getCategories().stream()
                                    .map(FeedbackCategoryEntity::getName)
                                    .collect(Collectors.toSet())
                    ))
                    .collect(Collectors.toList());
        }
        return null;
    }

    //해당 페이지의 피드백 확인
    public List<FeedbackDto> getFeedbacks(Long projectId, String page, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 사용자가 읽은 피드백 ID 목록
        List<Long> readFeedbackIds = feedbackReadRepository.findAllByUser(user).stream()
                .map(read -> read.getFeedback().getFeedbackId())
                .collect(Collectors.toList());

        return feedbackRepository.findByProject_ProjectIdAndPage(projectId, page)
                .stream()
                .filter(fb -> !readFeedbackIds.contains(fb.getFeedbackId()))
                .map(fb -> new FeedbackDto(
                        fb.getFeedbackId(),
                        fb.getProject().getProjectId(),
                        fb.getPage(),
                        fb.getX(),
                        fb.getY(),
                        fb.getText(),
                        fb.getAuthor().getUserName(),
                        fb.getCreatedAt(),
                        fb.getIsRead(),
                        //fb.getCategory()
                        fb.getCategories().stream()
                                .map(FeedbackCategoryEntity::getName)
                                .collect(Collectors.toSet())
                )).toList();
    }

    //특정 피드백 조회
    public FeedbackEntity getFeedbackById(Long userId, Long feedbackId, Long projectId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if(user.getRole() == UserEntity.UserRole.STUDENT) {
            return feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));
        }else if(user.getRole() == UserEntity.UserRole.PROFESSOR){
            return feedbackRepository.findByFeedbackIdAndProject_ProjectId(feedbackId, projectId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트의 피드백을 찾을 수 없습니다."));
        }
        return null;
    }

    //피드백 읽음 처리
    public void markFeedbackAsRead(Long userId, Long feedbackId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        FeedbackEntity feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));

        if (!feedbackReadRepository.existsByUserAndFeedback(user, feedback)) {
            FeedbackReadEntity read = new FeedbackReadEntity();
            read.setUser(user);
            read.setFeedback(feedback);
            read.setReadAt(LocalDateTime.now());
            feedback.setIsRead(Boolean.TRUE); //피드백을 읽었는지에 대한 여부를 명시
            feedbackReadRepository.save(read);
        }
    }

    //피드백 안읽음 처리
    public void markFeedbackAsDeleted(Long userId, Long feedbackId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        FeedbackEntity feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));

        feedback.setIsRead(Boolean.FALSE); //피드백을 안읽음으로 명시적 표현

        feedbackReadRepository.findByUserAndFeedback(user, feedback)
                .ifPresent(feedbackReadRepository::delete);
    }

    //피드백 삭제
    @Transactional
    public void deleteFeedback(Long feedbackId) {
        FeedbackEntity feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));


        feedbackRepository.delete(feedback);
    }

    // 피드백 내용 히스토리 저장
    private void saveFeedbackTextHistory(UserEntity professor, String text) {
        if (professor.getRole() != UserEntity.UserRole.PROFESSOR) {
            return;
        }
        Optional<FeedbackTextHistoryEntity> existingHistory = feedbackTextHistoryRepository.findByProfessor_UserIdAndText(professor.getUserId(), text);
        if (existingHistory.isPresent()) {
            FeedbackTextHistoryEntity history = existingHistory.get();
            history.setUsedAt(LocalDateTime.now());
            feedbackTextHistoryRepository.save(history);
        } else {
            FeedbackTextHistoryEntity newHistory = new FeedbackTextHistoryEntity();
            newHistory.setProfessor(professor);
            newHistory.setText(text);
            feedbackTextHistoryRepository.save(newHistory);
        }
    }

    // 피드백 내용 히스토리 조회
    public List<FeedbackTextHistoryDto> getFeedbackTextHistory(Long userId) {
        return feedbackTextHistoryRepository.findByProfessor_UserIdOrderByUsedAtDesc(userId)
                .stream()
                .map(history -> new FeedbackTextHistoryDto(history.getId(), history.getText(), history.getUsedAt()))
                .collect(Collectors.toList());
    }

    // -- 카테고리 관리 서비스 --

    @Transactional
    public FeedbackCategoryEntity createCategory(FeedbackCategoryDto dto, UserEntity user) {
        FeedbackCategoryEntity category = new FeedbackCategoryEntity();
        category.setName(dto.getName());
        category.setCreatedBy(user);
        return feedbackCategoryRepository.save(category);
    }

    public List<FeedbackCategoryDto> getCategories(UserEntity user) {
        // 교수는 자신이 생성한 카테고리만, 학생은 모든 교수의 카테고리를 볼 수 있도록 확장 가능 (현재는 교수 본인것만)
        return feedbackCategoryRepository.findAllByCreatedBy(user).stream()
                .map(cat -> new FeedbackCategoryDto(cat.getId(), cat.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedbackCategoryEntity updateCategory(Long categoryId, FeedbackCategoryDto dto, UserEntity user) {
        FeedbackCategoryEntity category = feedbackCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!category.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new SecurityException("자신이 생성한 카테고리만 수정할 수 있습니다.");
        }
        category.setName(dto.getName());
        return feedbackCategoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId, UserEntity user) {
        FeedbackCategoryEntity category = feedbackCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!category.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new SecurityException("자신이 생성한 카테고리만 삭제할 수 있습니다.");
        }
        feedbackCategoryRepository.delete(category);
    }

}
