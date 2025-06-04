package xyz.waranim.paymentservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.loolzaaa.youkassa.client.ApiClient;
import ru.loolzaaa.youkassa.client.ApiClientBuilder;
import xyz.waranim.paymentservice.entity.ShopCredentialEntity;
import xyz.waranim.paymentservice.repository.ShopCredentialRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final ShopCredentialRepository repo;
    private final Map<UUID, ApiClient> cache = new ConcurrentHashMap<>();

    public ApiClient getClient(UUID storeId) {
        return cache.computeIfAbsent(storeId, id -> {
            ShopCredentialEntity cred = repo.findByStoreId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Неизвестный магазин: " + id));
            return ApiClientBuilder.newBuilder()
                    .configureOAuth(cred.getOauthToken())
                    .build();
        });
    }
}
