package itmo.andrey.lab_backend.domain.dto;

import lombok.Data;

@Data
public class SignInDTO {

    private String name;
    private String password;
}
