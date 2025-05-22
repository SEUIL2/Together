package com.together.user;

import com.together.project.ProjectEntity;
import com.together.user.profile.dto.UserProfileResponseDto;
import com.together.user.profile.dto.UserProfileUpdateRequestDto;
import com.together.user.dto.UserSignUpRequestDto;
import com.together.user.professor.ProfessorEntity;
import com.together.user.professor.ProfessorRepository;
import com.together.user.student.StudentEntity;
import com.together.user.student.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;


    /** 회원가입
     *
     * 제공된 요청 정보를 바탕으로 새로운 사용자를 등록, 사용자는 학생(STUDENT) 또는 교수(PROFESSOR)일 수 있으며,
     * 해당 역할에 따라 정보를 저장
     *
     * 프론트에서 학생 or 교수를 선택했을때 해당 입력 필드를 보여주고
     * 입력이 완료되었을때 UserSignUpRequestDto에 해당하는 변수들을 서버로 보내주면 됨
     *
     * 현재(4월9일) 회원가입 시스템
     * 1. 회원가입 시 setEmailVerified 을 제외한 정보들을 입력하고 회원가입
     * 2. emailVerified 가 자동으로 false 상태가 됨
     * 3. EmailVerificationController{} 페이지의 sendVerificationEmail() 메소드에서 이메일 인증 시도
     * 4. EmailVerificationController{} 페이지의 verifyEmail() 메소드에서 이메일에 온 코드를 입력해서 성공하면
     * emailVerified 가 true 가 되면서 회원가입 완료
     *
     * @param requestDto {@link UserSignUpRequestDto}의 객체로, 사용자 가입 정보를 포함:
     *                   - userName: 사용자의 이름.
     *                   - userEmail: 사용자의 이메일 주소.
     *                   - userLoginId: 사용자의 로그인 ID.
     *                   - password: 사용자가 제공한 비밀번호.
     *                   - confirmPassword: 비밀번호 확인 값 (비밀번호 유효성 검사를 위해 필요).
     *                   - userRole: 사용자의 역할. "STUDENT" 또는 "PROFESSOR"만 허용됨.
     *                   - studentNumber: 학생 번호. 사용자 역할이 "STUDENT"인 경우 필수 입력 값.
     *                   - setEmailVerified : 이메일 인증이 되어있는지 안되어있는지
     * @throws IllegalArgumentException 다음의 경우 예외를 발생:
     *                                  - 비밀번호가 확인 비밀번호와 일치하지 않는 경우.
     *                                  - 역할이 "STUDENT"이고 학생 번호가 제공되지 않은 경우.
     *                                  - 제공된 사용자 역할이 "STUDENT" 또는 "PROFESSOR"가 아닌 경우.
     */
    @Transactional
    public void registerUser(UserSignUpRequestDto requestDto) {
        // 비밀번호 일치 여부 확인
        if (!requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // UserRole 확인
        if ("STUDENT".equals(requestDto.getUserRole())) {
            if (requestDto.getStudentNumber() == null || requestDto.getStudentNumber().isEmpty()) {
                throw new IllegalArgumentException("학생의 경우 학번이 필수입니다.");
            }

            StudentEntity student = new StudentEntity();
            student.setUserName(requestDto.getUserName());
            student.setUserEmail(requestDto.getUserEmail());
            student.setUserLoginId(requestDto.getUserLoginId());
            student.setPassword(encodedPassword);
            student.setRole(UserEntity.UserRole.STUDENT);
            student.setStudentNumber(requestDto.getStudentNumber()); // 학번 추가
//            student.setEmailVerified(false); //이메일 인증여부 false
            student.setEmailVerified(requestDto.isEmailVerified()); // <-- 수정 무영 추가


            studentRepository.save(student);
        } else if ("PROFESSOR".equals(requestDto.getUserRole())) {
            ProfessorEntity professor = new ProfessorEntity();
            professor.setUserName(requestDto.getUserName());
            professor.setUserEmail(requestDto.getUserEmail());
            professor.setUserLoginId((requestDto.getUserLoginId()));
            professor.setPassword(encodedPassword);
            professor.setRole(UserEntity.UserRole.PROFESSOR);
//            professor.setEmailVerified(false); //이메일 인증여부 false
            professor.setEmailVerified(requestDto.isEmailVerified()); // <-- 수정 무영 추가

            professorRepository.save(professor);
        } else {
            throw new IllegalArgumentException("올바른 사용자 유형이 아닙니다.");
        }
    }

    public Optional<UserEntity> findUserByUsername(String userLoginId){
        return userRepository.findByUserLoginId(userLoginId);
    }

    public Optional<UserEntity> findUserByEmail(String userEmail){
        return userRepository.findByUserEmail(userEmail);
    }

    //회원가입할때 로그인아이디 중복여부 확인
    public boolean isLoginIdDuplicated(String loginId) {
        return userRepository.existsByUserLoginId(loginId);
    }

    //교수 프로젝트 아이디 반환
    public List<Map<String, Object>> getProfessorProjectSummaries(Long userId) {
        ProfessorEntity professor = professorRepository.findById(userId).get();
        if (professor == null) {
            throw new EntityNotFoundException("교수님을 찾을수 없습니다.");
        }

        List<ProjectEntity> projects = professor.getProjects();

        List<Map<String, Object>> projectList = projects.stream().map(project -> {
            Map<String, Object> p = new HashMap<>();
            p.put("projectId", project.getProjectId());
            return p;
        }).toList();

        return projectList;
    }

    //유저 삭제
    @Transactional
    public void deleteUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

        // 🔹 공통 연관 정리
        // 프로젝트 팀장 리스트 제거
        for (ProjectEntity project : user.getProject()) {
            project.setLeader(null);
        }

        // 🔹 역할에 따라 연관 해제
        if (user instanceof StudentEntity student) {
            ProjectEntity mainProject = student.getMainProject();
            if (mainProject != null) {
                mainProject.getStudents().remove(student); // 🔄 양방향 제거
                student.setMainProject(null);
            }
        } else if (user instanceof ProfessorEntity professor) {
            for (ProjectEntity project : professor.getProjects()) {
                project.getProfessors().remove(professor); // 🔄 양방향 제거
            }
            professor.getProjects().clear(); // 교수 측에서도 비워줌
        }

        // 🔥 삭제
        userRepository.delete(user);
    }


}
