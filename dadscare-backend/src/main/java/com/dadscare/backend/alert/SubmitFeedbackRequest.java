package com.dadscare.backend.alert;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitFeedbackRequest(@NotNull Boolean wasCorrect, @Size(max = 500) String comment) {
}
