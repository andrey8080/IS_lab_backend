package itmo.andrey.lab1_backend.domain.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class SignUpDTO {
	private String name;
	private String password;
}
