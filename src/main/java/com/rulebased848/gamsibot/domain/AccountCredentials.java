package com.rulebased848.gamsibot.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class AccountCredentials {
    @NotBlank
    private String username;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[^_\\W]{8,}$", message = "Your password must be at least 8 characters long, contain at least one number, one uppercase letter and one lowercase letter.")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}