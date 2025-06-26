package mvc.domain.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResultDTO {
	private AuthStatus status;
	private Object data;
	
	public enum AuthStatus{
		LOGIN_SUCCESS,
		REGISTRATION_REQUIRED   
	}
}

