package com.together.ProjectDetail.develop;

import com.together.ProjectDetail.develop.dto.DevOrderItemResponseDto;
import com.together.ProjectDetail.develop.dto.DevOrderItemSaveRequestDto;
import com.together.ProjectDetail.develop.dto.DevOrderItemStatusUpdateDto;
import com.together.util.customAnnotation.CurrentProject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 기능별 개발 순서 API 컨트롤러
 */
@RestController
@RequestMapping("/dev-order-items")
@RequiredArgsConstructor
public class DevOrderItemController {

    private final DevOrderItemService devOrderItemService;

    // 모든 항목 조회
    @GetMapping
    public ResponseEntity<List<DevOrderItemResponseDto>> getDevOrderItems(@CurrentProject Long projectId) {
        List<DevOrderItemResponseDto> items = devOrderItemService.getDevOrderItems(projectId);
        return ResponseEntity.ok(items);
    }

    // 새 항목 추가
    @PostMapping
    public ResponseEntity<DevOrderItemResponseDto> addDevOrderItem(@CurrentProject Long projectId, @RequestBody DevOrderItemSaveRequestDto requestDto) {
        DevOrderItemResponseDto createdItem = devOrderItemService.addDevOrderItem(projectId, requestDto);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    // 항목 수정
    @PutMapping("/{itemId}")
    public ResponseEntity<DevOrderItemResponseDto> updateDevOrderItem(@PathVariable Long itemId, @RequestBody DevOrderItemSaveRequestDto requestDto) {
        DevOrderItemResponseDto updatedItem = devOrderItemService.updateDevOrderItem(itemId, requestDto);
        return ResponseEntity.ok(updatedItem);
    }

    // 항목 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteDevOrderItem(@PathVariable Long itemId) {
        devOrderItemService.deleteDevOrderItem(itemId);
        return ResponseEntity.noContent().build();
    }

    // '완료 여부'만 수정 (체크박스)
    @PatchMapping("/{itemId}/status")
    public ResponseEntity<DevOrderItemResponseDto> updateCompletedStatus(
            @PathVariable Long itemId,
            @RequestBody DevOrderItemStatusUpdateDto requestDto) {
        DevOrderItemResponseDto updatedItem = devOrderItemService.updateCompletedStatus(itemId, requestDto);
        return ResponseEntity.ok(updatedItem);
    }
}