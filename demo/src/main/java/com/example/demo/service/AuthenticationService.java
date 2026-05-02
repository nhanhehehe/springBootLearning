package com.example.demo.service;

import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.IntroSpectRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.dto.response.IntroSpectResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public IntroSpectResponse introspect(IntroSpectRequest request)
            throws JOSEException, ParseException {

        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        return IntroSpectResponse.builder()
                .valid(verified && expiredTime.after(new Date()))
                .build();
    }


    public AuthenticationResponse Authenticate(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean isAuthenticated = passwordEncoder.matches(authenticationRequest.getPassword(),
                user.getPassword());

        if (!isAuthenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .isAuthenticated(true)
                .build();
    }

     String generateToken(User user) {
         JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
         JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                 .subject(user.getUsername())
                 .issuer("devteria.com")
                 .issueTime(new Date())
                 .expirationTime(new Date(
                         Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                 ))
                 // claim ở đây trước dùng để demo, giờ dùng để define claim scope -> role
                 .claim("scope", buildScope(user))
                 .build();


         Payload payload = new Payload(jwtClaimsSet.toJSONObject());
         JWSObject jwsObject = new JWSObject(jwsHeader, payload);

         try {
             jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
             return  jwsObject.serialize();
         } catch (JOSEException e) {
             log.error("cannot create token! ", e);
             throw new RuntimeException(e);
         }
     }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(stringJoiner::add);
        }
        return stringJoiner.toString();
    }
}

