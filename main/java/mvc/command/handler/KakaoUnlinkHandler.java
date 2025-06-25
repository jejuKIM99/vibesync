package mvc.command.handler;

import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import mvc.command.service.KakaoAuthService;
import mvc.command.service.UserAuthService;
import mvc.domain.vo.UserVO;

public class KakaoUnlinkHandler implements CommandHandler {

    private UserAuthService userService = new UserAuthService();
    private KakaoAuthService kakaoService = new KakaoAuthService();

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userInfo") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(Map.of("success", false, "message", "로그인이 필요합니다.")));
            return null;
        }

        UserVO loginUser = (UserVO) session.getAttribute("userInfo");
        String accessToken = (String) session.getAttribute("kakaoAccessToken");

        // DB에서 연동 해제
        boolean success = userService.unlinkKakaoAccount(loginUser.getAc_idx());

        if (success) {
            // 카카오에 연결 끊기 API 호출
            if(accessToken != null) {
                kakaoService.kakaoUnlink(accessToken);
            }
            // 세션 정보 업데이트
            loginUser.setKakao_auth_id(null);
            // [수정됨] 아래 라인 삭제
            // loginUser.setLogin_provider("LOCAL"); 
            session.setAttribute("userInfo", loginUser);
            session.removeAttribute("kakaoAccessToken");

            out.print(gson.toJson(Map.of("success", true)));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("success", false, "message", "연동 해제 처리 중 오류가 발생했습니다.")));
        }
        out.flush();
        return null;
    }
}