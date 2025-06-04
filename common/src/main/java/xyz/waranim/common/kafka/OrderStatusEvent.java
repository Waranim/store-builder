package xyz.waranim.common.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusEvent {
    private String orderId;
    private String shopId;
    private String userEmail;
    private OrderStatus status;
    private Instant changedAt;
}
