package mvc.persistence.daoImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import mvc.domain.vo.UserDetailVO;
import mvc.persistence.dao.UserAccountDAO;

public class UserAccountDAOImpl implements UserAccountDAO {

    private Connection conn;

    public UserAccountDAOImpl(Connection conn) {
        this.conn = conn;
    }
    
    @Override
    public UserDetailVO getUserByKakaoId(long kakaoId) throws SQLException {
        String sql = "SELECT * FROM userAccount WHERE kakao_auth_id = ?";
        UserDetailVO user = null;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, kakaoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = UserDetailVO.builder()
                            .ac_idx(rs.getInt("ac_idx"))
                            .email(rs.getString("email"))
                            .pw(rs.getString("pw"))
                            .salt(rs.getString("salt"))
                            .nickname(rs.getString("nickname"))
                            .img(rs.getString("img"))
                            .name(rs.getString("name"))
                            .created_at(rs.getTimestamp("created_at"))
                            .category_idx(rs.getInt("category_idx"))
                            .kakao_auth_id(rs.getLong("kakao_auth_id"))
                            .build();
                }
            }
        }
        return user;
    }

    @Override
    public void linkKakaoAccount(int acIdx, long kakaoId) throws SQLException {
        String sql = "UPDATE userAccount SET kakao_auth_id = ? WHERE ac_idx = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, kakaoId);
            pstmt.setInt(2, acIdx);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void unlinkKakaoAccount(int acIdx) throws SQLException {
        String sql = "UPDATE userAccount SET kakao_auth_id = NULL WHERE ac_idx = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, acIdx);
            pstmt.executeUpdate();
        }
    }

    @Override
    public UserDetailVO getUserAccountById(int acIdx) throws SQLException {
        String sql = "SELECT * FROM userAccount WHERE ac_idx = ?";
        UserDetailVO user = null;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, acIdx);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = UserDetailVO.builder()
                            .ac_idx(rs.getInt("ac_idx"))
                            .email(rs.getString("email"))
                            .pw(rs.getString("pw"))
                            .salt(rs.getString("salt"))
                            .nickname(rs.getString("nickname"))
                            .img(rs.getString("img"))
                            .name(rs.getString("name"))
                            .created_at(rs.getTimestamp("created_at"))
                            .category_idx(rs.getInt("category_idx"))
                            .kakao_auth_id(rs.getLong("kakao_auth_id"))
                            .build();
                }
            }
        }
        return user;
    }

    @Override
    public void updateProfileImagePath(int acIdx, String newImagePath) throws SQLException {
        String sql = "UPDATE userAccount SET img = ? WHERE ac_idx = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newImagePath);
            pstmt.setInt(2, acIdx);
            pstmt.executeUpdate();
        }
    }
    
    @Override
    public void updatePassword(int acIdx, String newHashedPassword, String newSalt) throws SQLException {
        String sql = "UPDATE userAccount SET pw = ?, salt = ? WHERE ac_idx = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newHashedPassword);
            pstmt.setString(2, newSalt);
            pstmt.setInt(3, acIdx);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteAccount(int acIdx) throws SQLException {
        String sql = "DELETE FROM userAccount WHERE ac_idx = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, acIdx);
            pstmt.executeUpdate();
        }
    }
}