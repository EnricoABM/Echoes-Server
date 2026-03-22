package com.n0hana.echoes_server.dto;

public class PasswordDTO {

    public record ResetRequest(String email, String code, String newPassword, String confirmPassword) {}

    public record ResetResponse(String message) {}

    public record ForgotRequest(String email) {}

    public record ForgotResponse(String message) {}

    public record ValidateRequest(String password) {}

    public record ValidateResponse(String token) {}

    public record ChangeRequest(String token, String newPassword, String confirmPassword) {}

}
