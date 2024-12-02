package itmo.andrey.lab1_backend.domain.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class AdminResponseDTO {
    private String username;
    private String reason;
    private boolean isApproved;

    public AdminResponseDTO() {
        // Default constructor
    }

    public AdminResponseDTO(String username, String reason, boolean isApproved) {
        this.username = username;
        this.reason = reason;
        this.isApproved = isApproved;
    }

    public boolean getIsApproved() {
        return this.isApproved;
    }
}