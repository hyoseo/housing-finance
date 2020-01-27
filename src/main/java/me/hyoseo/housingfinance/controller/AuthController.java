package me.hyoseo.housingfinance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import javax.transaction.Transactional;
import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final UserRepository userRepository;

    private final CryptoService cryptoService;

    @Transactional
    @PostMapping("/signup")
    public ResponseEntity<AccessToken> signUp(@Valid @RequestBody IdPassword idPassword, Errors errors) {
        if (errors.hasErrors())
            throw CommonException.create(ErrorCode.BAD_REQUEST, errors.getFieldError().getDefaultMessage());

        String encPassword = cryptoService.encrypt(idPassword.getId(), idPassword.getPassword());
        if (userRepository.existsById(idPassword.getId()))
            throw CommonException.create(ErrorCode.ALREADY_EXIST_ID);

        userRepository.save(new User(idPassword.getId(), encPassword));

        return ResponseEntity.ok(new AccessToken(cryptoService.createToken(idPassword.getId())));
    }

    @PostMapping("/signin")
    public ResponseEntity<AccessToken> signIn(@RequestBody IdPassword idPassword) {
        User user = userRepository.findById(idPassword.getId())
                .orElseThrow(() -> CommonException.create(ErrorCode.ID_OR_PASSWORD_IS_WRONG));

        String encPassword = cryptoService.encrypt(idPassword.getId(), idPassword.getPassword());
        if (user.getPassword().equals(encPassword) == false)
            throw CommonException.create(ErrorCode.ID_OR_PASSWORD_IS_WRONG);

        return ResponseEntity.ok(new AccessToken(cryptoService.createToken(idPassword.getId())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessToken> refresh(@RequestHeader("Authorization") String authorization,
                                               @RequestAttribute("user_id") String userId) {
        if (authorization.equals("Bearer Token") == false)
            throw CommonException.create(ErrorCode.BAD_REQUEST);

        return ResponseEntity.ok(new AccessToken(cryptoService.createToken(userId)));
    }
}
