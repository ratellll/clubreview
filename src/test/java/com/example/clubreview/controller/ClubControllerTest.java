package com.example.clubreview.controller;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    @WithMockUser(username = "admin", roles = {"USER"})
    public void testListClubs() throws Exception {
        Page<Club> clubs = new PageImpl<>(List.of(club1, club2), PageRequest.of(0,10), 2);
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

    }
