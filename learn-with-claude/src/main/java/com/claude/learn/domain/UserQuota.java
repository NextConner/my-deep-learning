package com.claude.learn.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_quota")
public class UserQuota {

    @Id
    private String username;
    private int dailyLimit;
    private int usedToday;
    private LocalDate resetDate = LocalDate.now();

    public UserQuota() {}

    public String getUsername() { return username; }
    public int getDailyLimit() { return dailyLimit; }
    public int getUsedToday() { return usedToday; }
    public LocalDate getResetDate() { return resetDate; }

    public void addUsage(int tokens) { this.usedToday += tokens; }
    public void resetIfNewDay() {
        if (!resetDate.equals(LocalDate.now())) {
            this.usedToday = 0;
            this.resetDate = LocalDate.now();
        }
    }
    public boolean isExceeded() { return usedToday >= dailyLimit; }
}