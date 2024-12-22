package itmo.andrey.lab_backend.domain.dto;

import lombok.Data;

@Data
public class AdminRequestDTO {
    private String name;
    private String password;
    private boolean isAdmin;
    private String reason;
}
