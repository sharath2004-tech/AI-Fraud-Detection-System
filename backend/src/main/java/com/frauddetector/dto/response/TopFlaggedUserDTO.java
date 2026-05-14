package com.frauddetector.dto.response;

public class TopFlaggedUserDTO {
    private Long userId;
    private String name;
    private String email;
    private long flaggedCount;

    public TopFlaggedUserDTO(Long userId, String name, String email, long flaggedCount) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.flaggedCount = flaggedCount;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public long getFlaggedCount() { return flaggedCount; }
    public void setFlaggedCount(long flaggedCount) { this.flaggedCount = flaggedCount; }
}
