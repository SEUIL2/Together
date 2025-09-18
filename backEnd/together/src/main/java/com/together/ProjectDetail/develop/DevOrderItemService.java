package com.together.ProjectDetail.develop;

import com.together.ProjectDetail.develop.dto.DevOrderItemResponseDto;
import com.together.ProjectDetail.develop.dto.DevOrderItemSaveRequestDto;
import com.together.ProjectDetail.develop.dto.DevOrderItemStatusUpdateDto;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 기능별 개발 순서 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DevOrderItemService {

    private final ProjectRepository projectRepository;
    private final DevOrderItemRepository devOrderItemRepository;
    private final UserRepository userRepository; // 사용자 조회를 위해 UserRepository 주입

    // 새 항목 추가 메서드 수정
    @Transactional
    public DevOrderItemResponseDto addDevOrderItem(Long projectId, Long userId, DevOrderItemSaveRequestDto requestDto) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다. ID: " + projectId));
        UserEntity author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        DevOrderItemEntity newItem = new DevOrderItemEntity();
        newItem.setFeatureName(requestDto.getFeatureName());
        newItem.setFeatureOrder(requestDto.getFeatureOrder());
        newItem.setPriority(requestDto.getPriority());
        newItem.setDescription(requestDto.getDescription());
        newItem.setCompleted(requestDto.isCompleted());
        newItem.setProject(project);
        newItem.setAuthor(author); // 작성자 설정

        DevOrderItemEntity savedItem = devOrderItemRepository.save(newItem);
        return DevOrderItemResponseDto.fromEntity(savedItem);
    }

    // 항목 수정
    @Transactional
    public DevOrderItemResponseDto updateDevOrderItem(Long itemId, DevOrderItemSaveRequestDto requestDto) {
        DevOrderItemEntity item = devOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 항목을 찾을 수 없습니다. ID: " + itemId));

        item.setFeatureName(requestDto.getFeatureName());
        item.setFeatureOrder(requestDto.getFeatureOrder());
        item.setPriority(requestDto.getPriority());
        item.setDescription(requestDto.getDescription());
        item.setCompleted(requestDto.isCompleted());

        return DevOrderItemResponseDto.fromEntity(item);
    }

    // 항목 삭제
    @Transactional
    public void deleteDevOrderItem(Long itemId) {
        if (!devOrderItemRepository.existsById(itemId)) {
            throw new IllegalArgumentException("삭제할 항목을 찾을 수 없습니다. ID: " + itemId);
        }
        devOrderItemRepository.deleteById(itemId);
    }

    // 프로젝트의 모든 항목 조회
    public List<DevOrderItemResponseDto> getDevOrderItems(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다. ID: " + projectId);
        }

        // 여기를 수정했습니다!
        // devOrderItemRepository.findByProjectId(projectId) -> devOrderItemRepository.findByProject_ProjectId(projectId)
        List<DevOrderItemEntity> items = devOrderItemRepository.findByProject_ProjectId(projectId);

        return items.stream()
                .map(DevOrderItemResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // '완료 여부'만 수정
    @Transactional
    public DevOrderItemResponseDto updateCompletedStatus(Long itemId, DevOrderItemStatusUpdateDto requestDto) {
        DevOrderItemEntity item = devOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 항목을 찾을 수 없습니다. ID: " + itemId));

        item.setCompleted(requestDto.isCompleted());

        return DevOrderItemResponseDto.fromEntity(item);
    }
}