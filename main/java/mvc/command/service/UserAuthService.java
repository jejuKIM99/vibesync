package mvc.command.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;

import com.util.ConnectionProvider;
import com.util.JdbcUtil;

import mvc.domain.vo.UserDetailVO;
import mvc.persistence.dao.UserAccountDAO;
import mvc.persistence.daoImpl.UserAccountDAOImpl;

public class UserAuthService {

    private UserAccountDAO dao;

    // 카카오 ID로 사용자 조회
    public UserDetailVO getUserByKakaoId(long kakaoId) {
        Connection conn = null;
        UserDetailVO user = null;
        try {
            conn = ConnectionProvider.getConnection();
            dao = new UserAccountDAOImpl(conn);
            user = dao.getUserByKakaoId(kakaoId);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            JdbcUtil.close(conn);
        }
        return user;
    }

    // 계정에 카카오 연동
    public boolean linkKakaoAccount(int acIdx, long kakaoId) {
        Connection conn = null;
        boolean success = false;
        try {
            conn = ConnectionProvider.getConnection();
            conn.setAutoCommit(false);
            dao = new UserAccountDAOImpl(conn);

            // 이 카카오 ID로 연동된 계정이 있는지 확인
            UserDetailVO existingUser = dao.getUserByKakaoId(kakaoId);

            if (existingUser != null) {
                // [수정된 핵심 로직]
                // 이미 연동된 계정이 '자기 자신'인 경우 -> 연동 해제로 처리 (토글 기능)
                if (existingUser.getAc_idx() == acIdx) {
                    dao.unlinkKakaoAccount(acIdx);
                } else {
                    // '다른 사람'의 계정과 연동된 경우 -> 예외 발생
                    throw new Exception("This Kakao account is already linked to another user.");
                }
            } else {
                // 연동된 계정이 없는 경우 -> 신규 연동
                dao.linkKakaoAccount(acIdx, kakaoId);
            }
            
            conn.commit();
            success = true;
        } catch (Exception e) {
            JdbcUtil.rollback(conn);
            e.printStackTrace();
            // 핸들러가 이 예외를 처리할 수 있도록 false를 반환하기 전에 로그를 남기는 것이 좋습니다.
            // 이 경우, 핸들러에서 linkError 세션 속성을 설정하게 됩니다.
            return false;
        } finally {
            JdbcUtil.close(conn);
        }
        return success;
    }
    
    // 카카오 연동 해제
    public boolean unlinkKakaoAccount(int acIdx) {
        Connection conn = null;
        boolean success = false;
        try {

			conn = ConnectionProvider.getConnection();
			
            dao = new UserAccountDAOImpl(conn);
            dao.unlinkKakaoAccount(acIdx);
            success = true;
        } catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.close(conn);
        }
        return success;
    }
}