package com.n0hana.echoes_server.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public class ClassroomDTO {
    public record CreateResquest(@NotBlank String name, String description) {}

    public record EnrollRequest(@NotBlank String code) {}

    public record CreateContentResquest(@NotBlank String title, @NotBlank String body) {}

    public record Resposnse(UUID id, String name, String description, String code, String teacherName, Instant createdAt) {}

    public record Resposnse(UUID id, String title, String body, Instant createdAt) {}
}
