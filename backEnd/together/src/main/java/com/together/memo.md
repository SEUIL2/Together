
### 유저 - 교수 : Many To Many 로 수정 

- 테스트해야될 요소
1. User 테스트
   1. 유저 잘 만들어 지는지 테스트 (유저랑 교수 둘다 각각 잘 만들어지는지 테스트 필요함)
2. Project 테스트
   1. 프로젝트 잘 만들어지는지 테스트
   2. 프로젝트 만들고 자기자신이 초대되는지 확인할것 
   3. acceptInvitation , 초대수락 메서드 테스트
   4. getProjectMembers , 팀원조회 메서드 테스트 
3. CommentController (AOP 테스트)
   1. createComment 메소드 테스트 
4. GoogleDriveController (AOP 테스트)
   1. uploadFile, getFilesByProject 테스트 
5. MeetingController (AOP 테스트)
   1. createMeeting, getAllMeetings 테스트 
6. Notice (AOP 테스트)
   1. NoticeController - createNotice, getNoticesByProject 테스트
   2. NoticeService - createNotice 테스트 
7. DesignDetailController 메소드 전체 테스트 해봐야될듯 (project - ProjectDetail 패키지 안에있음 )
8. DevelopDetailController 메소드 전체 테스트 해봐야될듯 (project - ProjectDetail 패키지 안에있음 )
9. PlanningDetailController 메소드 전체 테스트 해봐야될듯 (project - ProjectDetail 패키지 안에있음 )