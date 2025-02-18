<img width="498" alt="Image" src="https://github.com/user-attachments/assets/2203426d-ab42-4fea-b545-0782a8940d64" height="300"/>

club 테이블
역할: 클럽(또는 장소)에 대한 정보를 저장.
 컬럼:
id (PK): 클럽을 식별하는 기본 키.
average_rating: 클럽의 평균 평점(리뷰에 기반).
call_number: 클럽의 전화번호.
description: 클럽 설명.
latitude, longitude: 클럽의 위치 좌표.
location: 클럽의 주소 또는 위치 정보.
name: 클럽의 이름.
photo_url: 클럽 사진 URL.


users 테이블
역할: 사용자 정보를 저장.
컬럼:
id (PK): 사용자를 식별하는 기본 키.
ban_end_time: 사용자 제한(정지) 종료 시간.
create_time: 계정 생성 시간.
nickname: 사용자의 닉네임.
password: 사용자의 암호.
phone_number: 사용자 전화번호.
role: 사용자 권한(예: admin, member 등).
username: 사용자 아이디 또는 로그인 이름.
 


review 테이블
역할: 클럽에 대한 사용자 리뷰를 저장.
컬럼:
id (PK): 리뷰를 식별하는 기본 키.
comment: 리뷰 내용.
create_time: 리뷰 작성 시간.
rating: 사용자 평점(정수형).
update_time: 리뷰 수정 시간.
club_id (FK): club 테이블과의 외래 키 관계.
user_id (FK): users 테이블과의 외래 키 관계.


테이블 간 관계
club와 review:
하나의 클럽은 여러 개의 리뷰를 가질 수 있는 1:N 관계.
users와 review:
한 명의 사용자가 여러 리뷰를 작성할 수 있는 1:N 관계.
-------------------------------------------------
 
 메서드 정리

UserController (RequestMapping("/users"))
POST | /register | 회원가입
GET  | /check | 회원가입시 통합 중복체크(username,phoneNumber,nickname)

AdminController (RequestMapping("/admin/users"))
GET | /list | 유저목록
POST | /delete/{id} | 유저탈퇴
POST | /ban/{id} | 유저정지
POST | /unban/{id} | 유저정지

ClubController (RequestMapping("/clubs"))
GET | /list | 클럽목록
GET | /{id} | 클럽상세
GET | /admin/new | 클럽생성폼이동
POST | /admin/new | 클럽생성처리
GET | /admin/edit/{id} | 클럽수정폼
POST | /admin/edit/{id} | 클럽수정처리
POST | /admin/delete/{id} | 클럽삭제처리

MyPageController (RequestMapping("/mypage"))
GET | /list | user정보와 review가져오는 마이페이지
POST | /editNickname | 닉네임수정처리
POST | /editPassword | 비밀번호수정처리
POST | /reviews/edit | 리뷰수정처리
POST | /reviews/delete/{id} | 리뷰삭제처리

ReviewController (RequestMapping("/reviews"))
POST | /add | 리뷰등록처리
POST | /user/edit/{id} | 리뷰수정처리
POST | /user/delete/{id} | 리뷰삭제처리
POST | /admin/edit/{id} | 어드민 리뷰수정처리
POST | /admin/delete/{id} |어드민 리뷰삭제처리

 