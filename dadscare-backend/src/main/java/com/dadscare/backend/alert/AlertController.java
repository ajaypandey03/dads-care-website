package com.dadscare.backend.alert;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public List<AlertDto> list() {
        return alertService.listForOrganization();
    }

    @PostMapping("/{id}/feedback")
    public void submitFeedback(@PathVariable Long id, @Valid @RequestBody SubmitFeedbackRequest request) {
        alertService.submitFeedback(id, request);
    }
}
