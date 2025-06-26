package mvc.command.handler;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import mvc.command.service.UserPageService; // 또는 NoteService 사용
import mvc.domain.dto.MorePostsDTO;

public class LoadMorePostsHandler implements CommandHandler {
    private UserPageService userPageService; // 또는 NoteService

    public LoadMorePostsHandler() {
        this.userPageService = new UserPageService();
    }

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try {
            int profileUserAcIdx = Integer.parseInt(request.getParameter("userId"));
            int pageNumber = Integer.parseInt(request.getParameter("page"));

            MorePostsDTO morePosts = userPageService.getMorePostsWithStatus(profileUserAcIdx, pageNumber);
            
            out.print(gson.toJson(morePosts));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", e.getMessage())));
            e.printStackTrace();
        } finally {
            if (out != null) out.flush();
        }
        return null; // JSON 직접 응답
    }
}