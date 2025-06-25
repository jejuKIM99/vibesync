package mvc.command.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mvc.command.service.KakaoAuthService;
import mvc.command.service.UserAuthService;
import mvc.domain.vo.UserDetailVO;
import mvc.domain.vo.UserVO;

public class KakaoCallbackHandler implements CommandHandler {

    private KakaoAuthService kakaoService = new KakaoAuthService();
    private UserAuthService userService = new UserAuthService();

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String code = request.getParameter("code");
        if (code == null) {
            request.setAttribute("error", "카카오 인증에 실패했습니다.");
            return "/WEB-INF/views/error.jsp"; // 에러 페이지로
        }

        try {
            // 1. 인가 코드로 액세스 토큰 받기
            String accessToken = kakaoService.getAccessToken(code);

            // 2. 액세스 토큰으로 카카오 사용자 정보 받기
            Map<String, Object> kakaoUserInfoMap = kakaoService.getUserInfo(accessToken);
            Number kakaoIdNumber = (Number) kakaoUserInfoMap.get("id");
            long kakaoId = kakaoIdNumber.longValue();

            HttpSession session = request.getSession();

            // 3. 계정 '연동' 플로우인지 확인
            String linkFlow = (String) session.getAttribute("kakaoLinkFlow");
            if ("true".equals(linkFlow)) {
                session.removeAttribute("kakaoLinkFlow"); // 플래그 제거
                UserVO loginUser = (UserVO) session.getAttribute("userInfo");
                
                boolean success = userService.linkKakaoAccount(loginUser.getAc_idx(), kakaoId);
                if(success) {
                    // 연동 성공 후 세션 정보 갱신
                    loginUser.setKakao_auth_id(kakaoId);
                    session.setAttribute("userInfo", loginUser);
                    session.setAttribute("kakaoAccessToken", accessToken); // 연동 후에도 토큰 저장
                } else {
                	// 이미 연동된 계정이라는 등의 에러 메시지를 userpage에 전달할 수 있음
                	session.setAttribute("linkError", "이미 다른 계정과 연동된 카카오 계정입니다.");
                }
                
                // 마이페이지의 설정으로 리다이렉트
                response.sendRedirect(request.getContextPath() + "/vibesync/userPage.do?acIdx=" + loginUser.getAc_idx() + "#settings");
                return null;
            }

            // 4. 일반 '로그인' 플로우
            UserDetailVO user = userService.getUserByKakaoId(kakaoId);

            if (user == null) {
                // 해당 카카오 ID로 가입된 회원이 없음 -> 회원가입 페이지로 유도
                // 카카오에서 받은 이메일, 닉네임 정보를 회원가입 폼에 미리 채워줄 수 있음
                // 여기서는 간단하게 로그인 페이지로 돌려보내며 에러 메시지 표시
                session.setAttribute("loginErrorForDisplay", "가입되지 않은 카카오 계정입니다. 먼저 계정을 연동해주세요.");
                response.sendRedirect(request.getContextPath() + "/vibesync/user.do");
                return null;
            }

            // 5. 로그인 성공 처리
            UserVO userVO = UserVO.builder()
                    .ac_idx(user.getAc_idx())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .img(user.getImg())
                    .name(user.getName())
                    .created_at(user.getCreated_at())
                    .category_idx(user.getCategory_idx())
                    .kakao_auth_id(user.getKakao_auth_id())
                    .build();

            session.setAttribute("userInfo", userVO);
            session.setAttribute("kakaoAccessToken", accessToken); // 로그아웃을 위해 토큰 저장

            response.sendRedirect(request.getContextPath() + "/vibesync/main.do");
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "카카오 로그인 처리 중 오류가 발생했습니다.");
            return "/WEB-INF/views/error.jsp";
        }
    }
}