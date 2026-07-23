package br.com.irrah.fiscal.auth.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.irrah.fiscal.auth.dto.LoginRequest;
import br.com.irrah.fiscal.auth.dto.LoginResponse;
import br.com.irrah.fiscal.security.JwtService;
import br.com.irrah.fiscal.usuario.domain.Usuario;
import br.com.irrah.fiscal.usuario.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse autenticar(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "E-mail ou senha inválidos."));

        if (!passwordEncoder.matches(
                request.senha(),
                usuario.getSenha())) {

            throw new BadCredentialsException(
                    "E-mail ou senha inválidos.");
        }

        String token = jwtService.gerarToken(usuario.getEmail());

        return new LoginResponse(
                token,
                "Bearer",
                usuario.getEmail()
        );
    }
}