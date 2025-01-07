EX) @PostMapping
public Club createClub(@RequestBody Club club){
return clubService.createClub(club);
} -- requestbody를 사용함으로서 JSON형태로 넘어오는 데이터를 Club객체로 받아옴

--- 중요 ---
security 공부
entity로 변환하는이유 builder를 사용하지않고
ResponseEntity<Boolean> 확인하기 
securiryConfig의 접근설정을 편히 하려면 users/register 로 나눠진 디렉토리들을 세분화해서 나눌필요가 있을듯 
* 왜냐하면 api가 추가될떄마다 접근설정을 다시해야할지도모름 복잡해짐
메서드 이름은 꼭 통일하자
 <Optional>
aws에 소스 업데이트방법

 

어드민
 

일반 


기타
Principal principal 
 컨트롤러쪽에 추가하여 안정성높이기
-clubController는 log추가할떄 principal 사용해서 추가하기
-adminController는 log추가할떄 principal 사용해서 추가하기
클럽-어드민-유저-리뷰-마이페이지 컨트롤러



@GetMapping("/list")
public String getMyPage(Principal principal, Model model) {
MyPageDto myPageData = myPageService.getMyPageData(principal.getName());
model.addAttribute("user", myPageData.getUser());
model.addAttribute("reviews", myPageData.getReviews());
return "mypage/list";
} 왜 객체로빼서 username을 넣지않고 principal로 바로 받는지 



 

