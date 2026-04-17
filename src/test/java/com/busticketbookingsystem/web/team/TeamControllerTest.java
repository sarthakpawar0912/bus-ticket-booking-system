package com.busticketbookingsystem.web.team;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
@Import(TeamRegistry.class)
class TeamControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void teamHome_renders() throws Exception {
        mockMvc.perform(get("/team"))
                .andExpect(status().isOk())
                .andExpect(view().name("team/members"))
                .andExpect(model().attributeExists("members"));
    }

    @Test
    void profile_rendersForSarthak() throws Exception {
        mockMvc.perform(get("/team/sarthak-pawar"))
                .andExpect(status().isOk())
                .andExpect(view().name("team/profile"))
                .andExpect(model().attributeExists("member"));
    }

    @Test
    void profile_rendersForEveryMember() throws Exception {
        String[] slugs = {"sarthak-pawar","atharv-kadam","atharva-pawar","anushka-bankar","kedar-mahadik"};
        for (String s : slugs) {
            mockMvc.perform(get("/team/" + s)).andExpect(status().isOk());
        }
    }

    @Test
    void profile_unknownSlug_redirects() throws Exception {
        mockMvc.perform(get("/team/unknown"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/team"));
    }
}
