package mvc.command.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class KakaoAuthService {

    // 애플리케이션 키 (중앙에서 관리하는 것이 좋음)
    private static final String REST_API_KEY = "5552b4df7e716e5ec496a4a58b2cf809"; // 본인 키로 교체
    private static final String REDIRECT_URI = "http://localhost:8081/vibesync/vibesync/auth/kakao/callback.do";

    // 1. 인가 코드로 액세스 토큰 받기
    public String getAccessToken(String code) throws Exception {
        String requestURL = "https://kauth.kakao.com/oauth/token";
        URL url = new URL(requestURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setDoOutput(true); // POST 요청을 위해 기본값이 false인 setDoOutput을 true로

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=").append(REST_API_KEY);
            sb.append("&redirect_uri=").append(REDIRECT_URI);
            sb.append("&code=").append(code);
            bw.write(sb.toString());
            bw.flush();
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            JsonObject jsonObject = new Gson().fromJson(result.toString(), JsonObject.class);
            return jsonObject.get("access_token").getAsString();
        }
    }

    // 2. 액세스 토큰으로 사용자 정보 받기
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserInfo(String accessToken) throws Exception {
        String requestURL = "https://kapi.kakao.com/v2/user/me";
        URL url = new URL(requestURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            return new Gson().fromJson(result.toString(), Map.class);
        }
    }

    // 3. 카카오 로그아웃
    public void kakaoLogout(String accessToken) throws Exception {
        String requestURL = "https://kapi.kakao.com/v1/user/logout";
        URL url = new URL(requestURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        
        // 응답 코드가 200이 아니더라도 예외를 발생시키지 않음 (토큰이 이미 만료되었을 수 있으므로)
        System.out.println("Kakao Logout Response Code: " + conn.getResponseCode());
    }
    
    // 4. 카카오 앱 연결 끊기 (회원 탈퇴, 연동 해제 시)
    public void kakaoUnlink(String accessToken) throws Exception {
    	 String requestURL = "https://kapi.kakao.com/v1/user/unlink";
         URL url = new URL(requestURL);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Authorization", "Bearer " + accessToken);
         
         System.out.println("Kakao Unlink Response Code: " + conn.getResponseCode());
    }
}