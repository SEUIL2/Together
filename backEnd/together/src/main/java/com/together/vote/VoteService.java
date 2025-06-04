package com.together.vote;

import com.together.notification.NotificationService;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.user.student.StudentEntity;
import com.together.vote.DTO.VoteDTO;
import com.together.vote.DTO.VoteDetailDTO;
import com.together.vote.DTO.VoteItemResultDTO;
import com.together.vote.entity.VoteEntity;
import com.together.vote.entity.VoteItemEntity;
import com.together.vote.entity.VoteResponseEntity;
import com.together.vote.repository.VoteItemRepository;
import com.together.vote.repository.VoteRepository;
import com.together.vote.repository.VoteResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private VoteResponseRepository voteResponseRepository;

    @Autowired
    private VoteItemRepository voteItemRepository;

    @Autowired
    private NotificationService notificationService;

    // 투표 생성
    public VoteEntity createVote(Long userId, Long projectId, VoteDTO voteDTO) {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);

        // 사용자가 존재하고 프로젝트가 존재하면
        if (userOptional.isPresent() && projectOptional.isPresent()) {
            UserEntity user = userOptional.get();
            ProjectEntity project = projectOptional.get();

            // 새로운 VoteEntity 생성 (투표 제목과 프로젝트, 사용자 설정)
            VoteEntity vote = new VoteEntity();
            vote.setTitle(voteDTO.getTitle()); // VoteDTO에서 제목을 가져와서 설정
            vote.setUser(user); // 작성자 설정 (UserEntity)
            vote.setProject(project); // 프로젝트 설정 (ProjectEntity)

            // 투표 항목 추가
            // VoteDTO의 항목들을 순회하면서 VoteItemEntity로 변환하여 추가
            for (String option : voteDTO.getOptions()) {
                // 각 항목에 대해 VoteItemEntity 생성
                VoteItemEntity voteItem = new VoteItemEntity();
                voteItem.setOptions(option); // 항목 내용 설정
                voteItem.setVote(vote);  // 해당 항목을 vote에 연결

                // 투표 항목 리스트에 항목 추가
                vote.getVoteItems().add(voteItem);
            }

            // ⭐ 알림 전송 코드 추가
            List<StudentEntity> projectUsers = project.getStudents();
            for (UserEntity notificationUser : projectUsers) {
                notificationService.sendNotification(
                        notificationUser.getUserId(),
                        "투표 등록",
                        vote.getTitle() + " 투표가 생성되었습니다."
                );
            }

            return voteRepository.save(vote);
        }

        return null;  // 사용자 또는 프로젝트가 없으면 null 반환
    }

    // 특정 프로젝트의 투표 조회
    public List<VoteEntity> getVotesByProject(Long projectId) {
        return voteRepository.findByProjectProjectId(projectId);
    }

    // 특정 투표 조회
    public VoteEntity getVoteById(Long voteId) {
        Optional<VoteEntity> voteOptional = voteRepository.findById(voteId);
        return voteOptional.orElse(null);  // 투표가 없으면 null 반환
    }

    // 투표 수정
    public VoteEntity updateVote(Long voteId, VoteDTO voteDTO) {
        Optional<VoteEntity> voteOptional = voteRepository.findById(voteId);

        if (voteOptional.isPresent()) {
            VoteEntity vote = voteOptional.get();
            vote.setTitle(voteDTO.getTitle());  // 투표 제목 수정

            // 기존 항목들을 삭제하고 새로운 항목 추가 (이 부분은 필요에 따라 수정)
            vote.getVoteItems().clear();
            for (String option : voteDTO.getOptions()) {
                VoteItemEntity voteItem = new VoteItemEntity();
                voteItem.setOptions(option);
                voteItem.setVote(vote);
                vote.getVoteItems().add(voteItem);
            }

            return voteRepository.save(vote);
        }

        return null;  // 투표가 존재하지 않으면 null 반환
    }

    // 투표 삭제
    public boolean deleteVote(Long voteId) {
        Optional<VoteEntity> voteOptional = voteRepository.findById(voteId);

        if (voteOptional.isPresent()) {
            voteRepository.delete(voteOptional.get());
            return true;
        }

        return false;  // 투표가 존재하지 않으면 삭제 실패
    }

    // 사용자 투표 응답 저장
    public VoteResponseEntity createVoteResponse(Long userId, Long voteId, Long voteItemId) {

        // 1. 이미 투표했는지 체크
        if (voteResponseRepository.existsByUserUserIdAndVoteVoteId(userId, voteId)) {
            throw new IllegalStateException("이미 이 투표에 참여했습니다.");
        }

        // 2. 기본 로직
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        Optional<VoteEntity> voteOptional = voteRepository.findById(voteId);
        Optional<VoteItemEntity> voteItemOptional = voteItemRepository.findById(voteItemId);

        if (userOptional.isPresent() && voteOptional.isPresent() && voteItemOptional.isPresent()) {
            UserEntity user = userOptional.get();
            VoteEntity vote = voteOptional.get();
            VoteItemEntity voteItem = voteItemOptional.get();

            VoteResponseEntity voteResponse = new VoteResponseEntity();
            voteResponse.setUser(user);
            voteResponse.setVote(vote);
            voteResponse.setVoteItem(voteItem);

            return voteResponseRepository.save(voteResponse);
        }

        return null;  // 사용자, 투표 또는 항목이 존재하지 않으면 null 반환
    }

    //투표 상세정보 반환
    public VoteDetailDTO getVoteWithResponses(Long voteId) {
        Optional<VoteEntity> voteOptional = voteRepository.findById(voteId);

        if (voteOptional.isPresent()) {
            VoteEntity vote = voteOptional.get();

            VoteDetailDTO voteDetailDTO = new VoteDetailDTO();
            voteDetailDTO.setVoteId(vote.getVoteId());
            voteDetailDTO.setTitle(vote.getTitle());
            voteDetailDTO.setCreatedDate(vote.getCreatedDate());

            List<VoteItemResultDTO> itemResults = vote.getVoteItems().stream().map(item -> {
                VoteItemResultDTO dto = new VoteItemResultDTO();
                dto.setVoteItemId(item.getVoteItemId());
                dto.setOptions(item.getOptions());
                dto.setResponseCount(item.getVoteResponse().size());  // 응답 수

                // 투표한 사용자 이름들 추출
                List<String> voterNames = item.getVoteResponse().stream()
                        .map(response -> response.getUser().getUserName())
                        .collect(Collectors.toList());

                dto.setVoterNames(voterNames);

                return dto;
            }).toList();

            voteDetailDTO.setItems(itemResults);
            return voteDetailDTO;
        }

        return null;
    }

}
