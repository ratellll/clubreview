EX) @PostMapping
public Club createClub(@RequestBody Club club){
return clubService.createClub(club);
} -- requestbody를 사용함으로서 JSON형태로 넘어오는 데이터를 Club객체로 받아옴

--- 중요 ---
security 공부
entity로 변환하는이유 builder를 사용하지않고


 어드민

-클럽삭제-
어드민은 리스트페이지에서 클럽이름앞에 체크할수있는게 보이고 체크한후 삭제버튼누르면
예아니오 버튼과 함께 예를 누르면 삭제될수있게

공통
클럽디테일
클럽디테일에 사진첨부된것도 볼수있게
 


 

