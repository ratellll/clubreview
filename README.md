EX) @PostMapping
public Club createClub(@RequestBody Club club){
return clubService.createClub(club);
} -- requestbody를 사용함으로서 JSON형태로 넘어오는 데이터를 Club객체로 받아옴


잘못된 아이디로 로그인시 얼러트창 띄우기
중복 회원가입 체크 


 어드민

-클럽등록- 
- 사진등록시 마커에서 못불러옴
- 사진등록시 이름중복 오류를 피하기위해 방법찾아야함

-클럽삭제-
어드민은 리스트페이지에서 클럽이름앞에 체크할수있는게 보이고 체크한후 삭제버튼누르면
예아니오 버튼과 함께 예를 누르면 삭제될수있게

-클럽수정-
클럽수정시 파일,주소 수정할수있게 (등록과 똑같이 위도경도 넘어오게만들어야함)


공통 

로그아웃


 

