package com.busticketbookingsystem.trip.controller;

import com.busticketbookingsystem.trip.entity.Route;
import com.busticketbookingsystem.trip.service.RouteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
class RouteControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private RouteService routeService;
    @Autowired private ObjectMapper objectMapper;

    private Route route;

    @BeforeEach
    void setUp() {
        route = new Route();
        route.setRouteId(1);
        route.setFromCity("Mumbai");
        route.setToCity("Pune");
        route.setBreakPoints(1);
        route.setDuration(180);
    }

    @Test
    void createRoute() throws Exception {
        when(routeService.create(any())).thenReturn(route);
        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(route)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routeId").value(1));
    }

    @Test
    void getAllRoutes() throws Exception {
        when(routeService.getAll()).thenReturn(List.of(route));
        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRouteById() throws Exception {
        when(routeService.getById(1)).thenReturn(route);
        mockMvc.perform(get("/api/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCity").value("Mumbai"));
    }

    @Test
    void updateRoute() throws Exception {
        when(routeService.update(eq(1), any())).thenReturn(route);
        mockMvc.perform(put("/api/routes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(route)))
                .andExpect(status().isOk());
    }



    @Test
    void searchRoutes() throws Exception {
        when(routeService.searchRoutes("Mumbai", "Pune")).thenReturn(List.of(route));
        mockMvc.perform(get("/api/routes/search").param("from", "Mumbai").param("to", "Pune"))
                .andExpect(status().isOk());
    }

    @Test
    void listRoutesView() throws Exception {
        when(routeService.getAll()).thenReturn(List.of(route));
        mockMvc.perform(get("/view/routes"))
                .andExpect(status().isOk())
                .andExpect(view().name("route/routes"))
                .andExpect(model().attributeExists("routes"));
    }

    @Test
    void showAddRouteForm() throws Exception {
        mockMvc.perform(get("/view/routes/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("route/add-route"))
                .andExpect(model().attributeExists("route"));
    }

    @Test
    void saveRouteView() throws Exception {
        when(routeService.create(any())).thenReturn(route);
        mockMvc.perform(post("/view/routes/save")
                        .param("fromCity", "Mumbai").param("toCity", "Pune")
                        .param("breakPoints", "1").param("duration", "180"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view/routes"));
    }


}
