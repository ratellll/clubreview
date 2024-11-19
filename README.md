EX) @PostMapping
public Club createClub(@RequestBody Club club){
return clubService.createClub(club);
} -- requestbody를 사용함으로서 JSON형태로 넘어오는 데이터를 Club객체로 받아옴


잘못된 아이디로 로그인시 얼러트창 띄우기
중복 회원가입 체크 


어드민일경우 로그인하고 리스트페이지에서 등록 삭제 페이지로 이동할수있게 버튼 등록

삭제페이지는 클럽들 검색할수있고 삭제할수있는거 
등록은 사진까지 등록할수있게
