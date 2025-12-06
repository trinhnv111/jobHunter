package trinhnv.jobOKO.service;

import jakarta.servlet.http.HttpServletRequest;
import trinhnv.jobOKO.domain.request.LoginRequest;
import trinhnv.jobOKO.domain.request.LoginResult;
import trinhnv.jobOKO.domain.entity.User;

public interface AuthService {
    LoginResult login(LoginRequest loginRequest, HttpServletRequest request);

    LoginResult refreshToken(String refreshToken);

    void logout();

    User getCurrentUser(String username);
}
