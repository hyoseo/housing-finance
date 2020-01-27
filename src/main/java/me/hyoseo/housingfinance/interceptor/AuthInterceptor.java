package me.hyoseo.housingfinance.interceptor;

import lombok.RequiredArgsConstructor;
import me.hyoseo.housingfinance.error.CommonException;
import me.hyoseo.housingfinance.error.ErrorCode;
import me.hyoseo.housingfinance.service.CryptoService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final CryptoService cryptoService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String accessToken = request.getHeader("Access-Token");
        if (accessToken == null)
            throw CommonException.create(ErrorCode.FORBIDDEN);

        String uid = cryptoService.parse(accessToken);
        request.setAttribute("user_id", uid);

        return true;
    }
}
