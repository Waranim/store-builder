package xyz.waranim.paymentservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.loolzaaa.youkassa.client.ApiClient;
import ru.loolzaaa.youkassa.client.ApiClientBuilder;
import xyz.waranim.paymentservice.entity.ShopCredentialEntity;
import xyz.waranim.paymentservice.repository.ShopCredentialRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock
    ShopCredentialRepository repo;
    @InjectMocks
    CredentialService service;

    @Test
    void getClient_returnsCachedInstance() {
        UUID storeId = UUID.randomUUID();
        String token = "tok_123";

        var cred = ShopCredentialEntity.builder()
                .storeId(storeId)
                .oauthToken(token)
                .build();

        when(repo.findByStoreId(storeId)).thenReturn(Optional.of(cred));

        try (MockedStatic<ApiClientBuilder> staticBuilder = mockStatic(ApiClientBuilder.class)) {
            ApiClientBuilder builder = mock(ApiClientBuilder.class);
            ApiClient api     = mock(ApiClient.class);

            staticBuilder.when(ApiClientBuilder::newBuilder).thenReturn(builder);
            when(builder.configureOAuth(token)).thenReturn(builder);
            when(builder.build()).thenReturn(api);

            ApiClient first = service.getClient(storeId);
            ApiClient second = service.getClient(storeId);

            assertSame(api, first);
            assertSame(first, second);
            verify(repo, times(1)).findByStoreId(storeId);
        }
    }

    @Test
    void getClient_unknownStore_throws() {
        UUID id = UUID.randomUUID();
        when(repo.findByStoreId(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getClient(id));
    }
}