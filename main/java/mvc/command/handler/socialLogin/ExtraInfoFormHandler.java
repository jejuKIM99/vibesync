package mvc.command.handler.socialLogin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mvc.command.handler.CommandHandler;

public class ExtraInfoFormHandler implements CommandHandler {

	@Override
	public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
		System.out.println(">ExtraInfoFormHandler 호출됨");
		
		HttpSession session = request.getSession(false);
		
		if (session == null || session.getAttribute("newGoogleUser") == null) {
			response.sendRedirect(request.getContextPath()+"/vibesync/user.do");
			return null;
		}
		
		
		request.setAttribute("formToShow", "googleExtraInfo");
		return "/vibesync/login.jsp";
	}

}
