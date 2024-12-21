package itmo.andrey.lab_backend.domain.dto;

import lombok.Data;

@Data
public class AdminResponseDTO {
    private String username;
    private String reason;
    private boolean isApproved;

    public AdminResponseDTO() {}

    public AdminResponseDTO(String username, String reason, boolean isApproved) {
        this.username = username;
        this.reason = reason;
        this.isApproved = isApproved;
    }

    public boolean getIsApproved() {
        return this.isApproved;
    }
}