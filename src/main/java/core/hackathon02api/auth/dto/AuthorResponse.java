package core.hackathon02api.auth.dto;

import lombok.*;

@Getter
@Setter
public class AuthorResponse {
    private Long id;
    private String nickname;
    private String roadAddress;
}
