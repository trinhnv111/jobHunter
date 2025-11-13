package trinhnv.springRestfull.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import trinhnv.springRestfull.domain.ApiResponse;
import trinhnv.springRestfull.domain.dto.LoginDTO;
import trinhnv.springRestfull.domain.dto.ResLoginDTO;
import trinhnv.springRestfull.util.SecurityUtil;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;

    //  Dùng AuthenticationManager, KHÔNG dùng AuthenticationManagerBuilder
    public AuthController(AuthenticationManager authenticationManager,SecurityUtil securityUtil) {
       this.authenticationManager = authenticationManager;
       this.securityUtil = securityUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login( @Valid  @RequestBody LoginDTO loginDTO) {

        //Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        //xác thực người dùng => cần viết hàm loadUserByUsername

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        String accessToken = this.securityUtil.createToken(authentication);

        ResLoginDTO res = new ResLoginDTO();
        res.setToken(accessToken);

        return ResponseEntity.ok().body(res);
    }
}
