package me.hyoseo.housingfinance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.hyoseo.housingfinance.database.model.User;
import me.hyoseo.housingfinance.database.repository.UserRepository;
import me.hyoseo.housingfinance.error.CommonException;
import me.hyoseo.housingfinance.error.ErrorCode;
import me.hyoseo.housingfinance.request.IdPassword;
import me.hyoseo.housingfinance.response.AccessToken;
import me.hyoseo.housingfinance.service.CryptoService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.validation.Valid;

@Api(tags = {"계정 컨트롤러"})
@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final UserRepository userRepository;

    private final CryptoService cryptoService;

    private final EntityManager entityManager;

    @ApiOperation(value = "회원가입", response = ResponseEntity.class)
    @Transactional
    @PostMapping("/signup")
    public ResponseEntity<AccessToken> signUp(@Valid @RequestBody IdPassword idPassword, Errors errors) {
        if (errors.hasErrors())
            throw CommonException.create(ErrorCode.BAD_REQUEST, errors.getFieldError().getDefaultMessage());

        String encPassword = cryptoService.encrypt(idPassword.getId(), idPassword.getPassword());
        if (userRepository.existsById(idPassword.getId()))
            throw CommonException.create(ErrorCode.ALREADY_EXIST_ID);

        try {
            entityManager.persist(new User(idPassword.getId(), encPassword));
            entityManager.flush();
        } catch (PersistenceException e) {
            throw CommonException.create(ErrorCode.ALREADY_EXIST_ID, e);
        }

        return ResponseEntity.ok(new AccessToken(cryptoService.createToken(idPassword.getId())));
    }

    @ApiOperation(value = "로그인", response = ResponseEntity.class)
    @PostMapping("/signin")
    public ResponseEntity<AccessToken> signIn(@Valid @RequestBody IdPassword idPassword, Errors errors) {
        if (errors.hasErrors())
            throw CommonException.create(ErrorCode.BAD_REQUEST, errors.getFieldError().getDefaultMessage());

        User user = userRepository.findById(idPassword.getId())
                .orElseThrow(() -> CommonException.create(ErrorCode.ID_OR_PASSWORD_IS_WRONG));

        String encPassword = cryptoService.encrypt(idPassword.getId(), idPassword.getPassword());
        if (user.getPassword().equals(encPassword) == false)
            throw CommonException.create(ErrorCode.ID_OR_PASSWORD_IS_WRONG);

        return ResponseEntity.ok(new AccessToken(cryptoService.createToken(idPassword.getId())));
    }

    @ApiOperation(value = "JWT 갱신", response = ResponseEntity.class)
    @ApiImplicitParams(@ApiImplicitParam(name = "Access-Token", value = "Access-Token 필요", paramType = "header"))
    @PostMapping("/refresh")
    public ResponseEntity<AccessToken> refresh(@RequestHeader("Authorization") String authorization,
                                               @RequestAttribute("user_id") String userId) {
        if (authorization.equals("Bearer Token") == false)
            throw CommonException.create(ErrorCode.BAD_REQUEST);

        return ResponseEntity.ok(new AccessToken(cryptoService.createToken(userId)));
    }
}
