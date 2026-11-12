package br.com.jmcodestudio.agendador.auth;

import br.com.jmcodestudio.agendador.fotografo.FotografoResponse;

public record AuthResponse(String token, FotografoResponse fotografo) {
}
