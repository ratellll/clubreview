package com.example.clubreview.controller;

import com.example.clubreview.dto.ClubDto;
import com.example.clubreview.entity.Club;
import com.example.clubreview.service.ClubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ClubController.class)
class ClubControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClubService clubService;

    private Club club1;
    private Club club2;


    @BeforeEach
    void setUp() {
        club1 = Club.builder()
                .name("Club A")
                .location("Location A")
                .description("Description A")
                .callNumber("123-4567")
                .averageRating(4.5)
                .build();

        club2 = Club.builder()
                .name("Club B")
                .location("Location B")
                .description("Description B")
                .callNumber("789-0123")
                .averageRating(3.8)
                .build();
    }

    //목록 조회 테스트
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testListClubs() throws Exception {
        Page<Club> clubs = new PageImpl<>(List.of(club1, club2), PageRequest.of(0, 10), 2);
        Mockito.when(clubService.getClubsSortedByName(anyInt(), anyInt())).thenReturn(clubs);

        mockMvc.perform(MockMvcRequestBuilders.get("/clubs")
                        .param("sortBy", "name")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("clubs/list"))
                .andExpect(model().attributeExists("clubs"))
                .andDo(print());
    }

    //상세조회
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void 클럽상세조회() throws Exception {
        Mockito.when(clubService.getClubById(any(Long.class))).thenReturn(club1);
        mockMvc.perform(MockMvcRequestBuilders.get("/clubs/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("clubs/details"))
                .andExpect(model().attributeExists("club"))
                .andDo(print());
    }


    //클럽생성폼
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void 클럽생성폼() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/clubs/admin/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("clubs/create"))
                .andExpect(model().attributeExists("club"))
                .andDo(print());
    }


    //클럽생성처리
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void 클럽생성처리() throws Exception {
        //클럽dto만들기
        ClubDto clubDto = new ClubDto();
        clubDto.setName("Club C");
        clubDto.setLocation("Location C");
        clubDto.setDescription("Description C");
        clubDto.setCallNumber("111-2222");
        clubDto.setAverageRating(4.5);

        Mockito.doNothing().when(clubService).addClub(any(ClubDto.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/clubs/admin/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", clubDto.getName())
                        .param("location", clubDto.getLocation())
                        .param("description", clubDto.getDescription())
                        .param("callNumber", clubDto.getCallNumber())
                        .param("averageRating", String.valueOf(clubDto.getAverageRating())))
                .andExpect(status().is3xxRedirection())  // 생성 후 리다이렉션 확인
                .andExpect(view().name("redirect:/clubs/list"));  // 리다이렉션 경로 확인
    }
}


