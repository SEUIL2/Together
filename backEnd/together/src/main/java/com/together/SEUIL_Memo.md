
### 유저 - 교수 : Many To Many 로 수정 

- 테스트해야될 요소

- CustomAnnotation - CurrentProject 만들었음
    - 프론트에서 projectId 아이디를 할당받는데 
    - 학생의 경우에는 projectId 아이디를 할당받지 않아도 현재의 프로젝트를 자동으로 가져옴
    - 교수의 경우에는 할당받은 projectId 를 가져감 

1. User 테스트 **(테스트완료 - 학생 완료 , 교수는 수정필요)**
   1. 유저 잘 만들어 지는지 테스트 (유저랑 교수 둘다 각각 잘 만들어지는지 테스트 필요함) - 완료(근데 교수를 만들었을때 상속되는 ProfessorEntity 가 안만들어짐 확인해봐야될듯 )
   
2. Project 테스트 **(테스트완료 - 학생 완료 , 교수는 수정 필요)**
   1. Controller - createProject, getMyProject, inviteUser, getProjectMembers, getProjectMembers 테스트 (완료)
   2. 프로젝트 만들고 자기자신이 자동으로 초대되는지 확인할것 (완료)
   3. acceptInvitation , 초대수락 메서드 테스트 (완료 - 학생은 되는데 교수가 안됨) 

3. CommentController **(테스트완료)**
   1. createComment 메소드 테스트 
   
4. GoogleDriveController **(테스트완료)**
   1. uploadFile, getFilesByProject 테스트 
   
5. MeetingController **(테스트완료)**
   1. createMeeting, getAllMeetings 테스트 
   
6. Notice **(테스트 완료)**
   1. NoticeController - createNotice, getNoticesByProject 테스트
   2. NoticeService - createNotice 테스트 
   
7. DesignDetailController 메소드 전체 테스트 해봐야될듯 (project - ProjectDetail 패키지 안에있음 ) **(테스트완료)**

8. DevelopDetailController 메소드 전체 테스트 해봐야될듯 (project - ProjectDetail 패키지 안에있음 ) **(테스트완료)**

9. PlanningDetailController 메소드 전체 테스트 해봐야될듯 (project - ProjectDetail 패키지 안에있음 ) **(테스트완료)**

10. Vote **(테스트 완료)**
    1. VoteController - createVote, getVotesByProject 테스트 
    2. VoteService - createNotice 알림전송코드 테스트 

- 테스트 다 끝나고 교수 초대부터 만들어야될듯

---

### 교수기능 
1. 피드백 플로팅 위젯 → 피그마 기능 (프론트 만들어지면 이후에 작업예정)
2. 공지사항 작성 **(테스트완료)**
3. 투표 작성 **(테스트완료)**
4. 학생 메모 (프론트 만들어지면 이후에 작업예정)
5. 나머지는 보는것만 

- 프로젝트 초대 **(테스트완료)**

---

### 알림기능 
1. 프로젝트 초대 _ ProjectService.java -> inviteUserToProject() **(테스트완료)**
2. 공지사항 등록 _ NoticeService.java -> createNotice() **(테스트완료)**
3. 투표 등록 _ VoteService.java -> createVote() **(테스트완료)**
4. 작업 할당 (예정)
5. 교수 피드백 (예정)

---

### AI 기능 
1. 키워드 8개 출력 **(테스트완료)**
2. 키워드 5개 입력하면 주제 추천 **(테스트완료)**