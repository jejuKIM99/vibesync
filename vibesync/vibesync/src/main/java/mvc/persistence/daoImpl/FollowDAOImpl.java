package mvc.persistence.daoImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.util.JdbcUtil;

import mvc.domain.dto.SidebarDTO;
import mvc.domain.vo.UserSummaryVO;
import mvc.domain.vo.UserVO;
import mvc.persistence.dao.FollowDAO;

public class FollowDAOImpl implements FollowDAO {
    private Connection conn = null;

    // 생성자
    public FollowDAOImpl(Connection conn) {
    	this.conn = conn;
    }
    
	// 팔로워 목록
	@Override
	public List<UserSummaryVO> userFollowerList(int ac_idx) {
		List<UserSummaryVO> users = new ArrayList<>();
		
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    
	    String sql = " SELECT " + 
   			 " u.ac_idx, u.nickname, u.img, u.category_idx, " +
   			 " CASE " +
   			 " 		WHEN f_reverse.ac_following IS NOT NULL THEN 1 " +
   			 " 		ELSE 0 " +
   			 " END AS followedByCurrentUser " +
   			 " FROM follows f " +
   			 " JOIN userAccount u ON u.ac_idx = f.ac_follow " +
   			 " LEFT JOIN follows f_reverse ON f_reverse.ac_follow = f.ac_following " +
   			 " 							   AND f_reverse.ac_following = f.ac_follow " +
   			 " WHERE f.ac_following = ? ";
	    
	    try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, ac_idx);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				UserSummaryVO user = UserSummaryVO.builder()
										  .ac_idx(rs.getInt("ac_idx"))
										  .nickname(rs.getString("nickname"))
										  .profile_img(rs.getString("img"))
										  .category_idx(rs.getInt("category_idx"))
										  .followedByCurrentUser(rs.getInt("followedByCurrentUser")==1)
										  .build();
				
				users.add(user);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(pstmt);
		}
		
		return users;
	}
	
	// 팔로잉 목록
	@Override
	public List<UserSummaryVO> userFollowingList(int ac_idx) {
		List<UserSummaryVO> users = new ArrayList<>();
		
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    
	    String sql = "SELECT " + 
	    			 " ac_idx, nickname, img, category_idx " +
	    			 "FROM follows f JOIN userAccount u ON u.ac_idx = f.ac_following " +
	    			 "WHERE ac_follow = ?";
	    
	    try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, ac_idx);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				UserSummaryVO user = UserSummaryVO.builder()
										  .ac_idx(rs.getInt("ac_idx"))
										  .nickname(rs.getString("nickname"))
										  .profile_img(rs.getString("img"))
										  .category_idx(rs.getInt("category_idx"))
										  .followedByCurrentUser(true)
										  .build();
				users.add(user);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(pstmt);
		}
		
		return users;
	}
	
    // 팔로잉 목록 : 유저가 팔로우하고 있는 사용자 ID
	@Override
	public List<Integer> userFollowingIdList(int ac_idx) {
		List<Integer> userIdList = new ArrayList<>();
		
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    
	    String sql = "SELECT " + 
	    			 " ac_following " +
	    			 " FROM follows " +
	    			 " WHERE ac_follow = ?";
	    
	    try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, ac_idx);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				userIdList.add(rs.getInt("ac_following"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(pstmt);
		}
		
		return userIdList;
	}
    
	// 전체 카테고리 - 인기 유저 조회
	@Override
	public List<UserSummaryVO> findPopularUsers(int limit) throws SQLException {
		 List<UserSummaryVO> users = new ArrayList<>();
		 
	     PreparedStatement pstmt = null;
	     ResultSet rs = null;
		 
	     // 인기 유저: (받은 좋아요 수 + 팔로워 수) 합산 기준, Oracle TOP-N 쿼리
	     String sql = "SELECT ac_idx, email, nickname, img, name, category_idx " +
	                " FROM ( " +
	                "    SELECT " +
	                "        ua.ac_idx, " + 
	                "		 ua.email,	" +
	                "        ua.nickname, " +
	                "        ua.img, " +
	                "        ua.name, " +
	                "        ua.category_idx, " +
	                "        COALESCE(follower_counts.total_followers, 0) AS popularity_score, " + // 인기도 점수를 팔로워 수로만 계산
	                "        ROW_NUMBER() OVER (ORDER BY COALESCE(follower_counts.total_followers, 0) DESC, ua.created_at DESC) as rn " +
	                "    FROM " +
	                "        userAccount ua " +
	                "    LEFT JOIN " +
	                "        (SELECT ac_following, COUNT(follows_idx) AS total_followers " +
	                "         FROM follows " +
	                "         GROUP BY ac_following) follower_counts ON ua.ac_idx = follower_counts.ac_following " +
	                " ) " +
	                " WHERE rn <= ? ";

	     try {
	    	 pstmt = conn.prepareStatement(sql);
	         pstmt.setInt(1, limit);
	         rs = pstmt.executeQuery();

	         while (rs.next()) {
	        	 UserSummaryVO user = UserSummaryVO.builder()
	        			 				   .ac_idx(rs.getInt("ac_idx"))
	        			 				   .nickname(rs.getString("nickname"))
	        			 				   .profile_img(rs.getString("img"))
	        			 				   .category_idx(rs.getInt("category_idx"))
	        			 				   .build();
	        			 
	             users.add(user);
	         }
	      
	      } catch (Exception e) {
	    	  e.printStackTrace();
	      } finally {
	    	  if (rs != null) rs.close();
	    	  if (pstmt != null) pstmt.close();
	      }
	        
	      return users;
	}

	// 특정 카테고리의 인기 유저 조회
	@Override
	public List<UserSummaryVO> findPopularUsersByCategory(int categoryIdx, int limit) throws SQLException {
		 List<UserSummaryVO> users = new ArrayList<>();
		 
	     PreparedStatement pstmt = null;
	     ResultSet rs = null;
		 
	     String sql = " SELECT * " +
	    		 	" FROM ( " + 
	    		 	"	SELECT " +
	    		 	"		 u.ac_idx, " + 
	    		 	"		 u.email, " + 
	    		 	"		 u.nickname, " + 
	    		 	"		 u.img, " + 
	    		 	"		 u.name, " + 
	    		 	"		 COUNT(f.ac_follow) AS follower_count " + 
	                " 	FROM " + 
	    		 	"		userAccount u " +
	    		 	" 	LEFT JOIN follows f ON u.ac_idx = f.ac_following " +
	                " 	WHERE " + 
	    		 	" 		u.category_idx = ? " + 
	    		 	" 	GROUP BY " +
	                "		u.ac_idx, u.email, u.nickname, u.img, u.name, u.category_idx " + 
	                "	ORDER BY " + 
	                "		follower_count DESC " + 
	                " ) " +
	                " WHERE ROWNUM <= ? ";

	     try {
	    	 pstmt = conn.prepareStatement(sql);
	         pstmt.setInt(1, categoryIdx);
	         pstmt.setInt(2, limit);
	         rs = pstmt.executeQuery();

	         while (rs.next()) {
	        	 UserSummaryVO user = UserSummaryVO.builder()
		 				   .ac_idx(rs.getInt("ac_idx"))
		 				   .nickname(rs.getString("nickname"))
		 				   .profile_img(rs.getString("img"))
		 				   .category_idx(categoryIdx)
		 				   .build();
	        	 
	             users.add(user);
	         }
	      
	      } catch (Exception e) {
	    	  e.printStackTrace();
	      } finally {
	    	  if (rs != null) rs.close();
	    	  if (pstmt != null) pstmt.close();
	      }
	        
	      return users;
	}
	
	// 팔로우
	 @Override
    public int addFollow(int followerAcIdx, int followingAcIdx) throws SQLException {
        String sql = "INSERT INTO follows (follows_idx, ac_follow, ac_following) VALUES (follows_seq.NEXTVAL, ?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, followerAcIdx);
            pstmt.setInt(2, followingAcIdx);
            return pstmt.executeUpdate();
        } finally {
            if (pstmt != null) pstmt.close();
        }
    }

	// 팔로우 취소
    @Override
    public int removeFollow(int followerAcIdx, int followingAcIdx) throws SQLException {
        String sql = "DELETE FROM follows WHERE ac_follow = ? AND ac_following = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, followerAcIdx);
            pstmt.setInt(2, followingAcIdx);
            return pstmt.executeUpdate();
        } finally {
            if (pstmt != null) pstmt.close();
        }
    }

    // 팔로잉 여부 확인
    @Override
    public boolean isFollowing(int followerAcIdx, int followingAcIdx) throws SQLException {
        String sql = "SELECT COUNT(*) FROM follows WHERE ac_follow = ? AND ac_following = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, followerAcIdx);
            pstmt.setInt(2, followingAcIdx);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }

    /*

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // (2) SQL: follows 테이블과 userAccount 테이블을 조인해서 ac_following 사용자의 정보를 가져온다.
        String sql = ""
            + "SELECT u.ac_idx, u.email, u.nickname, u.img, u.name, u.created_at, u.category_idx "
            + "  FROM follows f "
            + "  JOIN userAccount u ON f.ac_following = u.ac_idx "
            + " WHERE f.ac_follow = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, acFollow);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                UserVO uvo = UserVO.builder()
                                   .ac_idx(rs.getInt("ac_idx"))
                                   .email(rs.getString("email"))
                                   .nickname(rs.getString("nickname"))
                                   .img(rs.getString("img"))
                                   .name(rs.getString("name"))
                                   .created_at(rs.getDate("created_at"))
                                   .category_idx(rs.getInt("category_idx"))
                                   .build();
                followingList.add(uvo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // (8) 자원 해제
            try { if (rs != null)    rs.close();    } catch (Exception e) { }
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) { }
        }

        // (9) SidebarDTO에 리스트 담아서 반환
        SidebarDTO dto = new SidebarDTO();
        dto.setFollowingList(followingList);
        return dto;
    }
    */
    
    @Override
    public int getFollowerCount(int userAcIdx) throws SQLException {
        String sql = "SELECT COUNT(*) FROM follows WHERE ac_following = ?"; // userAcIdx를 팔로우하는 사람들의 수
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userAcIdx);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
        return count;
    }

    @Override
    public int getFollowingCount(int userAcIdx) throws SQLException {
        String sql = "SELECT COUNT(*) FROM follows WHERE ac_follow = ?"; // userAcIdx가 팔로우하는 사람들의 수
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userAcIdx);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
        return count;
    }

}
