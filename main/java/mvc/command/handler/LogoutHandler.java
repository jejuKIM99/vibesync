package mvc.command.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import mvc.command.service.KakaoAuthService;

public class LogoutHandler implements CommandHandler {

    private KakaoAuthService kakaoService = new KakaoAuthService();

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession(false);

        if (session != null) {
            // 카카오 로그아웃 처리
            String accessToken = (String) session.getAttribute("kakaoAccessToken");
            if (accessToken != null) {
                try {
                    kakaoService.kakaoLogout(accessToken);
                } catch (Exception e) {
                    e.printStackTrace(); // 카카오 로그아웃 실패가 전체 로그아웃을 막아서는 안됨
                }
            }
            // 로컬 세션 무효화
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + "/vibesync/user.do?accessType=logout");
        return null;
    }
}