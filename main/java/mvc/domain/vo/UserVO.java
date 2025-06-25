// UserVO.java 전체 코드
package mvc.domain.vo;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserVO {

	private int ac_idx;
	private String email;
	private String nickname;
	private String img;
	private String name;
	private Timestamp created_at;
	private int category_idx;
	
	// [추가된 필드]
	private Long kakao_auth_id; 
}