package com.dadscare.backend.alert;

import com.dadscare.backend.tenant.TenantContext;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final FeedbackEntryRepository feedbackEntryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AlertDto> listForOrganization() {
        return alertRepository.findAllByOrganizationIdOrderByCreatedAtDesc(TenantContext.organizationId()).stream()
                .map(AlertDto::from)
                .toList();
    }

    @Transactional
    public void submitFeedback(Long alertId, SubmitFeedbackRequest request) {
        Long organizationId = TenantContext.organizationId();
        Alert alert = alertRepository
                .findByIdAndOrganizationId(alertId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Alert " + alertId + " not found"));
        User submitter = userRepository
                .findByIdAndOrganizationId(TenantContext.userId(), organizationId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));

        FeedbackEntry feedback = new FeedbackEntry();
        feedback.setAlert(alert);
        feedback.setSubmittedBy(submitter);
        feedback.setWasCorrect(request.wasCorrect());
        feedback.setComment(request.comment());
        feedbackEntryRepository.save(feedback);
    }
}
