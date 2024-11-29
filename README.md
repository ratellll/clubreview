EX) @PostMapping
public Club createClub(@RequestBody Club club){
return clubService.createClub(club);
} -- requestbody를 사용함으로서 JSON형태로 넘어오는 데이터를 Club객체로 받아옴

--- 중요 ---
security 공부
entity로 변환하는이유 builder를 사용하지않고

 
 


 

