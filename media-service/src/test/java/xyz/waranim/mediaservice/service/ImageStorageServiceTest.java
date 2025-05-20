package xyz.waranim.mediaservice.service;

import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import xyz.waranim.mediaservice.config.MinioProps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageStorageServiceTest {

    @Mock
    MinioClient minio;

    @Mock
    MinioProps props;

    @InjectMocks
    ImageStorageService service;

    @BeforeEach
    void setUp() {
        when(props.getBucket()).thenReturn("images");
    }

    @Test
    void upload_ok() throws Exception {
        byte[] bytes = { 1, 2, 3 };
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", bytes);

        String objectKey = service.upload(file);

        assertThat(objectKey).endsWith("-test.png");

        ArgumentCaptor<PutObjectArgs> captor =
                ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minio, times(1)).putObject(captor.capture());

        PutObjectArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo("images");
        assertThat(args.object()).isEqualTo(objectKey);
        assertThat(args.contentType()).isEqualTo("image/png");
    }

    @Test
    void download_ok() throws Exception {
        byte[] expected = "hello".getBytes();
        GetObjectResponse resp = mock(GetObjectResponse.class);
        when(resp.readAllBytes()).thenReturn(expected);
        when(minio.getObject(any(GetObjectArgs.class))).thenReturn(resp);

        byte[] actual = service.download("abc-123");

        assertThat(actual).isEqualTo(expected);
        verify(minio, times(1)).getObject(any(GetObjectArgs.class));
        verify(resp, times(1)).close();
    }

    @Test
    void delete_ok() throws Exception {
        service.delete("abc-123");

        verify(minio, times(1)).removeObject(any(RemoveObjectArgs.class));
    }
}