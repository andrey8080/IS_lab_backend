package itmo.andrey.lab1_backend.domain;

import itmo.andrey.lab1_backend.domain.dto.AdminRequestDTO;
import itmo.andrey.lab1_backend.domain.dto.SignUpDTO;
import lombok.Getter;

@Getter
public class SignUpRequest {
    private SignUpDTO formData;
    private AdminRequestDTO adminRequestData;

}
