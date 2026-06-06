package com.ms.user.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodigoCacheService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> expiracoes = new ConcurrentHashMap<>();

    public void salvarCodigo(String email, String codigo) {
        cache.put(email, codigo);
        expiracoes.put(email, System.currentTimeMillis() + 300000);
    }

    public String obterCodigo(String email) {
        return cache.get(email);
    }

    @Scheduled(fixedRate = 60000)
    public void limparCodigosExpirados() {
        long agora = System.currentTimeMillis();
        expiracoes.entrySet().removeIf(entry -> {
            if (entry.getValue() < agora) {
                cache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
}