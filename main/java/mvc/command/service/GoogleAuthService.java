package mvc.command.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.util.Collections;
import java.util.UUID;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.util.Config;
import com.util.ConnectionProvider;
import com.util.JdbcUtil;

import mvc.domain.dto.AuthResultDTO;
import mvc.domain.dto.AuthResultDTO.AuthStatus;
import mvc.domain.dto.SignUpDTO;
import mvc.domain.dto.UserDTO;
import mvc.domain.vo.UserVO;
import mvc.persistence.dao.UserDAO;
import mvc.persistence.daoImpl.UserDAOImpl;

public class GoogleAuthService {
	
	 private static final String GOOGLE_CLIENT_ID = Config.getGoogleClientId();

	public AuthResultDTO verifyAndLoginWithGoogle(String id_token) {
		Connection conn = null;
		UserDAO userDAO = null;
				
		try {
			conn = ConnectionProvider.getConnection();
			userDAO = new UserDAOImpl(conn);
			
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
					.setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
					.build();
			
			GoogleIdToken idToken = null;
			try {
			// 2. verifier.verify() 메소드로 토큰 문자열 검증
			idToken = verifier.verify(id_token);
		}catch(GeneralSecurityException | IOException e){
			System.out.println("토큰 검증 중 예외 발생: " + e.getMessage());
			return null;
		}
		
		if (idToken != null) {
			// 3. 검증 성공, 토큰에서 사용자 정보 추출
			Payload payload = idToken.getPayload();
			String email = payload.getEmail();
			String name = (String) payload.get("name");
			
			UserVO userVO = userDAO.findByEmail(email);          
	         
	         if (userVO == null ) {
	        	 // 신규 사용자
	        	System.out.println("신규 사용자. 추가 정보 입력이 필요합니다.");
				SignUpDTO newUser = new SignUpDTO();
				newUser.setEmail(email);
				newUser.setName(name);
				 // 소셜 로그인이므로 비밀번호는 랜덤 값으로 설정
				newUser.setPassword(UUID.randomUUID().toString());
				newUser.setLoginType("google");
				
				// AuthResult 객체에 상태와 임시 사용자 정보를 담아 반환
				return new AuthResultDTO(AuthStatus.REGISTRATION_REQUIRED, newUser);
				
			} else {
				// 기존사용자일 경우
				System.out.println("기존 사용자입니다. 로그인을 진행합니다."); 
				return new AuthResultDTO(AuthStatus.LOGIN_SUCCESS, userVO);
			}      		
		}
	} catch(Exception e ) {
		e.printStackTrace();
	} finally {
		if (conn != null) try { JdbcUtil.close(conn); } catch(Exception e) {e.printStackTrace();}
	}
		return null;
}
}
