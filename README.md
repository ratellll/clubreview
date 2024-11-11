EX) @PostMapping
public Club createClub(@RequestBody Club club){
return clubService.createClub(club);
} -- requestbody를 사용함으로서 JSON형태로 넘어오는 데이터를 Club객체로 받아옴


잘못된 아이디로 로그인시 얼러트창 띄우기
중복 회원가입 체크 
