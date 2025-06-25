package mvc.command.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KakaoLinkHandler implements CommandHandler {
    
    private static final String REST_API_KEY = "5552b4df7e716e5ec496a4a58b2cf809"; // 본인 키로 교체
    private static final String REDIRECT_URI = "http://localhost:8081/vibesync/vibesync/auth/kakao/callback.do";

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 계정 연동 플로우임을 세션에 기록
        request.getSession().setAttribute("kakaoLinkFlow", "true");
        
        String location = "https://kauth.kakao.com/oauth/authorize?client_id=" + REST_API_KEY 
                        + "&redirect_uri=" + REDIRECT_URI 
                        + "&response_type=code&prompt=login";
        
        response.sendRedirect(location);
        return null;
    }
}