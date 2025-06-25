package mvc.command.handler.socialLogin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

import mvc.command.handler.CommandHandler;
import mvc.command.service.GoogleAuthService;
import mvc.domain.dto.AuthResultDTO;
import mvc.domain.dto.SignUpDTO;
import mvc.domain.dto.UserDTO;
import mvc.domain.vo.UserVO;

public class GoogleCallbackHandler implements CommandHandler {
	
	private GoogleAuthService googleAuthService = new GoogleAuthService();
	
	@Override
	public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
		System.out.println("> GoogleCallbackHandler.process() 호출됨");
		
		// 1. AJAX가 보낸 'id_token' 파라미터 값 받기
		String id_token = request.getParameter("id_token");
		
		if (id_token == null) {
			// code없는 비정상적 접근
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		//2. 서비스 계층 - 토큰 검증
		AuthResultDTO authResult = googleAuthService.verifyAndLoginWithGoogle(id_token);
		
		// JSON 응답을 위한 설정
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		
		JsonObject jsonResponse = new JsonObject();
		
		if (authResult == null) {
			System.err.println("[GoogleCallbackHandler] 오류: GoogleAuthService가 null을 반환했습니다.");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Server Error
			jsonResponse.addProperty("status", "error");
			jsonResponse.addProperty("message", "Authentication service failed.");
			response.getWriter().write(jsonResponse.toString());
		}
		
		// 3. 로그인 성공 시 세션에 저장
		switch (authResult.getStatus()) {
		case LOGIN_SUCCESS:
			UserVO loginUser = (UserVO) authResult.getData();
			HttpSession session = request.getSession();
			session.setAttribute("userInfo", loginUser);
			jsonResponse.addProperty("status", "success");
			break;
		
		case REGISTRATION_REQUIRED:
			SignUpDTO newUser = (SignUpDTO) authResult.getData();
			// 임시 정보를 세션에 저장하여 다음 단계로 전달
			request.getSession().setAttribute("newGoogleUser", newUser);
			// 추가 정보 입력 페이지로 이동
			String redirectUrl = request.getContextPath()+"/member/extraInfo.do";
			
			jsonResponse.addProperty("status", "extra_info_required");
			jsonResponse.addProperty("redirect_url", redirectUrl);
			break;
		}
		
		response.getWriter().write(jsonResponse.toString());
		
		return null;
	}

}
