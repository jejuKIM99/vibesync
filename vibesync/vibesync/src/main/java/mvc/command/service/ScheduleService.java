package mvc.command.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.util.ConnectionProvider;
import com.util.JdbcUtil;

import mvc.domain.dto.CalendarEventDTO;
import mvc.domain.vo.ScheduleVO;
import mvc.persistence.dao.ScheduleDAO;
import mvc.persistence.daoImpl.ScheduleDAOImpl;

public class ScheduleService {

    // 기본 생성자
    public ScheduleService() {
    }

    /**
     * 특정 월의 일정들을 조회하여 CalendarEventDTO 반환.
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 캘린더 이벤트 DTO 리스트
     * @throws Exception
     */
    public List<CalendarEventDTO> getMonthlySchedules(int acIdx, Timestamp start, Timestamp end) throws Exception {
        Connection conn = null;
        try {
            conn = ConnectionProvider.getConnection(); 
            ScheduleDAO scheduleDAO = new ScheduleDAOImpl(conn);

            // 1. DAO로부터 DB 데이터(VO)를 받습니다.
            List<ScheduleVO> schedulesFromDB = scheduleDAO.findSchedulesByRange(acIdx, start, end);

            // 2. 클라이언트에 보낼 DTO 리스트를 새로 생성합니다.
            List<CalendarEventDTO> eventsForJson = new ArrayList<>();

            // 3. VO 리스트를 DTO 리스트로 변환합니다.
            for (ScheduleVO vo : schedulesFromDB) { 
                CalendarEventDTO dto = CalendarEventDTO.builder()
                        .schedule_idx(vo.getSchedule_idx())
                        .title(vo.getTitle())
                        // Date 객체를 ISO 8601 형식의 문자열로 변환 (FullCalendar 호환)
                        .start(vo.getStart_time().toInstant().toString()) // Timestamp -> Instant -> String
                        .end(vo.getEnd_time().toInstant().toString())   // Timestamp -> Instant -> String
                        .color(vo.getColor())
                        .ac_idx(vo.getAc_idx())
                        .description(vo.getDescription())
                        .durationEditable(true)
                        .allDay(false)
                        .build();
                eventsForJson.add(dto);
            }

            return eventsForJson; // description이 포함된 DTO 리스트를 반환합니다.

        } finally {
            if (conn != null) {
                try {
                	JdbcUtil.close(conn);
                } catch (Exception e) {
                    System.err.println("DB 연결 닫는 중 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    public List<ScheduleVO> getDailySchedules(int acIdx, String dateStr) throws Exception {
        Connection conn = null;
        try {
            conn = ConnectionProvider.getConnection();
            ScheduleDAO scheduleDAO = new ScheduleDAOImpl(conn);
            return scheduleDAO.findSchedulesByDate(acIdx, dateStr);
        } catch (SQLException | NamingException e) {
            e.printStackTrace(); 
            throw e;
        } finally {
            if (conn != null) {
                JdbcUtil.close(conn);
            }
        }
    }
    
    public boolean addSchedule(ScheduleVO schedule) throws Exception {
        Connection conn = null;
        try {
            conn = ConnectionProvider.getConnection();
            conn.setAutoCommit(false);

            ScheduleDAO scheduleDAO = new ScheduleDAOImpl(conn);
            int addedRows = scheduleDAO.addSchedule(schedule);

            if (addedRows > 0) {
                conn.commit();
                return true;
            }
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) JdbcUtil.close(conn);
        }
        return false;
    }
    
    public boolean updateSchedule(ScheduleVO schedule) throws Exception {
        Connection conn = null;
        try {
            conn = ConnectionProvider.getConnection();
            conn.setAutoCommit(false);

            ScheduleDAO scheduleDAO = new ScheduleDAOImpl(conn);
            int updatedRows = scheduleDAO.updateSchedule(schedule);

            if (updatedRows > 0) {
                conn.commit();
                return true;
            }
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) JdbcUtil.close(conn);
        }
        return false;
    }
    
    public boolean deleteSchedule(int scheduleIdx) throws Exception {
        Connection conn = null;
        try {
            conn = ConnectionProvider.getConnection();
            conn.setAutoCommit(false);

            ScheduleDAO scheduleDAO = new ScheduleDAOImpl(conn);
            int deletedRows = scheduleDAO.deleteSchedule(scheduleIdx);

            if (deletedRows > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) JdbcUtil.close(conn);
        }
    }
}