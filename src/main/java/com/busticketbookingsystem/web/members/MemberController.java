package com.busticketbookingsystem.web.members;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberRegistry registry;
    private final OperationExecutor executor;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${server.port:8080}")
    private String serverPort;

    public MemberController(MemberRegistry registry, OperationExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    // ---------- Listing ----------

    @GetMapping
    public String listMembers(Model model) {
        model.addAttribute("members", registry.getAll());
        return "members/members";
    }

    // ---------- Member detail (services + operations) ----------

    @GetMapping("/{id}")
    public String memberDetail(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return registry.findById(id).map(m -> {
            model.addAttribute("member", m);
            return "members/member-detail";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Member not found with id " + id);
            return "redirect:/members";
        });
    }

    // ---------- Operation form ----------

    @GetMapping("/{id}/operation")
    public String showOperationForm(@PathVariable Integer id,
                                    @RequestParam String service,
                                    @RequestParam String operation,
                                    Model model,
                                    RedirectAttributes ra) {
        Member member = registry.findById(id).orElse(null);
        Operation op = registry.findOperation(id, service, operation).orElse(null);
        if (member == null || op == null) {
            ra.addFlashAttribute("error", "Operation not found.");
            return "redirect:/members/" + id;
        }
        model.addAttribute("member", member);
        model.addAttribute("serviceKey", service);
        model.addAttribute("operation", op);
        return "members/operation";
    }

    // ---------- Execute operation ----------

    @PostMapping("/{id}/operation/execute")
    public String executeOperation(@PathVariable Integer id,
                                   @RequestParam String service,
                                   @RequestParam String operation,
                                   @RequestParam(required = false) String pathId,
                                   @RequestParam Map<String, String> allParams,
                                   Model model,
                                   RedirectAttributes ra) {
        Member member = registry.findById(id).orElse(null);
        Operation op = registry.findOperation(id, service, operation).orElse(null);
        if (member == null || op == null) {
            ra.addFlashAttribute("error", "Operation not found.");
            return "redirect:/members/" + id;
        }

        // PDF download requires a direct link (not a form submit), but handle defensively:
        if ("PDF_DOWNLOAD".equals(op.getInputKind())) {
            if (pathId == null || pathId.isBlank()) {
                model.addAttribute("error", "ID is required to download the PDF.");
                model.addAttribute("member", member);
                model.addAttribute("serviceKey", service);
                model.addAttribute("operation", op);
                return "members/operation";
            }
            return "redirect:" + op.getEndpoint().replace("{id}", pathId.trim());
        }

        // PDF download with query params (e.g. group ticket)
        if ("PDF_DOWNLOAD_QUERY".equals(op.getInputKind())) {
            Map<String, String> q = new HashMap<>(allParams);
            q.remove("service");
            q.remove("operation");
            q.remove("pathId");
            StringBuilder qs = new StringBuilder();
            for (Map.Entry<String, String> e : q.entrySet()) {
                String v = e.getValue();
                if (v == null || v.isBlank()) continue;
                if (qs.length() > 0) qs.append('&');
                qs.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                  .append('=')
                  .append(URLEncoder.encode(v, StandardCharsets.UTF_8));
            }
            return "redirect:" + op.getEndpoint() + (qs.length() > 0 ? "?" + qs : "");
        }

        // Strip out routing params from the form data so only DTO fields remain
        Map<String, String> formData = new HashMap<>(allParams);
        formData.remove("service");
        formData.remove("operation");
        formData.remove("pathId");

        OperationExecutor.ExecutionResult result = executor.execute(op, pathId, formData);

        model.addAttribute("member", member);
        model.addAttribute("serviceKey", service);
        model.addAttribute("operation", op);
        model.addAttribute("submittedPathId", pathId);
        model.addAttribute("submittedForm", formData);
        model.addAttribute("result", result);
        return "members/operation";
    }

    // ---------- Quick PDF download proxy (for the direct Download button) ----------

    @GetMapping("/{id}/operation/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Integer id,
                                              @RequestParam String service,
                                              @RequestParam String operation,
                                              @RequestParam String pathId,
                                              HttpServletRequest req) {
        Operation op = registry.findOperation(id, service, operation).orElse(null);
        if (op == null || !"PDF_DOWNLOAD".equals(op.getInputKind())) {
            return ResponseEntity.notFound().build();
        }
        String url = "http://localhost:" + serverPort + op.getEndpoint().replace("{id}", pathId);
        byte[] pdf = restTemplate.getForObject(url, byte[].class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ticket-" + pathId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
