package com.busticketbookingsystem.trip.controller;

import com.busticketbookingsystem.trip.entity.Route;
import com.busticketbookingsystem.trip.service.RouteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@SuppressWarnings("java:S4684")
@Controller
public class RouteController {

    private static final String ATTR_MESSAGE = "message";
    private static final String ATTR_ERROR = "error";
    private static final String REDIRECT_VIEW_ROUTES = "redirect:/view/routes";

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    // ======================== REST API ========================

    @PostMapping("/api/routes")
    @ResponseBody
    public ResponseEntity<Route> createRoute(@RequestBody Route route) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.create(route));
    }

    @GetMapping("/api/routes")
    @ResponseBody
    public List<Route> getAllRoutes() {
        return routeService.getAll();
    }

    @GetMapping("/api/routes/{id}")
    @ResponseBody
    public Route getRouteById(@PathVariable Integer id) {
        return routeService.getById(id);
    }

    @PutMapping("/api/routes/{id}")
    @ResponseBody
    public Route updateRoute(@PathVariable Integer id, @RequestBody Route route) {
        return routeService.update(id, route);
    }

    @DeleteMapping("/api/routes/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteRoute(@PathVariable Integer id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/routes/search")
    @ResponseBody
    public List<Route> searchRoutes(@RequestParam String from, @RequestParam String to) {
        return routeService.searchRoutes(from, to);
    }

    // ======================== THYMELEAF VIEWS ========================

    @GetMapping("/view/routes")
    public String listRoutes(Model model) {
        model.addAttribute("routes", routeService.getAll());
        return "route/routes";
    }

    @GetMapping("/view/routes/add")
    public String showAddForm(Model model) {
        model.addAttribute("route", new Route());
        return "route/add-route";
    }

    @PostMapping("/view/routes/save")
    public String saveRoute(@ModelAttribute Route route, RedirectAttributes ra) {
        try {
            routeService.create(route);
            ra.addFlashAttribute(ATTR_MESSAGE, "Route added successfully!");
        } catch (Exception ex) {
            ra.addFlashAttribute(ATTR_ERROR, ex.getMessage());
        }
        return REDIRECT_VIEW_ROUTES;
    }

    @GetMapping("/view/routes/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("route", routeService.getById(id));
        return "route/update-route";
    }

    @PostMapping("/view/routes/update/{id}")
    public String updateRoute(@PathVariable Integer id, @ModelAttribute Route route, RedirectAttributes ra) {
        try {
            routeService.update(id, route);
            ra.addFlashAttribute(ATTR_MESSAGE, "Route updated successfully!");
        } catch (Exception ex) {
            ra.addFlashAttribute(ATTR_ERROR, ex.getMessage());
        }
        return REDIRECT_VIEW_ROUTES;
    }

    @GetMapping("/view/routes/search")
    public String searchRoutesView(@RequestParam(required = false) String from,
                                   @RequestParam(required = false) String to,
                                   Model model) {
        if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
            model.addAttribute("results", routeService.searchRoutes(from.trim(), to.trim()));
            model.addAttribute("from", from);
            model.addAttribute("to", to);
        }
        return "route/search-routes";
    }

    @GetMapping("/view/routes/delete/{id}")
    public String deleteRouteView(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            routeService.delete(id);
            ra.addFlashAttribute(ATTR_MESSAGE, "Route deleted successfully!");
        } catch (Exception ex) {
            ra.addFlashAttribute(ATTR_ERROR, ex.getMessage());
        }
        return REDIRECT_VIEW_ROUTES;
    }
}
