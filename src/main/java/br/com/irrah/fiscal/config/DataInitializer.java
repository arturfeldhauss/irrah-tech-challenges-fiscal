package br.com.irrah.fiscal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.irrah.fiscal.usuario.domain.Perfil;
import br.com.irrah.fiscal.usuario.domain.Usuario;
import br.com.irrah.fiscal.usuario.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        cadastrarUsuario(
                "Admin ERP",
                "admin@erpvarejo.com",
                "Admin@123",
                Perfil.ADMIN
        );

        cadastrarUsuario(
                "Operador Caixa 01",
                "caixa01@erpvarejo.com",
                "User@123",
                Perfil.OPERADOR
        );

        cadastrarUsuario(
                "Operador Caixa 02",
                "caixa02@erpvarejo.com",
                "User@123",
                Perfil.OPERADOR
        );
    }

    private void cadastrarUsuario(
            String nome,
            String email,
            String senha,
            Perfil perfil) {

        if (usuarioRepository.existsByEmail(email)) {
            return;
        }

        Usuario usuario = new Usuario(
                nome,
                email,
                passwordEncoder.encode(senha),
                perfil
        );

        usuarioRepository.save(usuario);
    }
}