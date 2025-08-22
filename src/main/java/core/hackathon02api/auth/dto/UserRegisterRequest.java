package core.hackathon02api.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UserRegisterRequest {

    private String username;
    private String password;
    private String nickname;
    private String gender;
    private String ageRange;
    private String roadAddress;
    private String interestsJson;

}
