EX) @PostMapping
public Club createClub(@RequestBody Club club){
return clubService.createClub(club);
} -- requestbody를 사용함으로서 JSON형태로 넘어오는 데이터를 Club객체로 받아옴

--- 중요 ---
security 공부
entity로 변환하는이유 builder를 사용하지않고

Error : 마커클릭시 커스텀정보창이 제대로안보임
클럽 클릭시 로그인해야함
 


 

