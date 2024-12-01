package itmo.andrey.lab1_backend.domain.dto;

import lombok.Data;

@Data
public class SignUpDTO {

	private String name;
	private String password;
	private boolean candidateAdmin;
}
