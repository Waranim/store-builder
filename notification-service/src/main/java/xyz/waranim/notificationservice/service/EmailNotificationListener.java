package xyz.waranim.notificationservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import xyz.waranim.common.kafka.OrderStatus;
import xyz.waranim.common.kafka.OrderStatusEvent;
import xyz.waranim.notificationservice.dto.ShopDto;
import xyz.waranim.notificationservice.dto.UserResponse;
import xyz.waranim.notificationservice.feign.AuthClient;
import xyz.waranim.notificationservice.feign.ShopClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EmailNotificationListener {

    private final static String TEMPLATE_MAIL_TEXT = """
            Статус заказа %s изменился
            Актуальный статус: %s
            """;
    private static final String TEMPLATE_MAIL_FROM = "noreply@%s";
    private final Map<OrderStatus, String> orderStatusMap = new HashMap<>();

    private final JavaMailSender mailSender;
    private final ShopClient shopClient;
    private final AuthClient authClient;

    @KafkaListener(topics = "order.status.changed", groupId = "email")
    public void onEvent(OrderStatusEvent evt) {
        ShopDto shop = shopClient.getById(UUID.fromString(evt.getShopId()));
        UserResponse user = authClient.getUser(shop.ownerId());
        sendNotification(evt, evt.getUserEmail(), shop.name());
        sendNotification(evt, user.email(), shop.name());
    }

    private void sendNotification(OrderStatusEvent evt, String emailAddress, String shopName) {
        String subject = "Ваш заказ в магазине " + shopName + " — " + orderStatusMap.get(evt.getStatus());
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(String.format(TEMPLATE_MAIL_FROM, evt.getShopId()));
        msg.setTo(emailAddress);
        msg.setSubject(subject);
        msg.setText(String.format(TEMPLATE_MAIL_TEXT, evt.getOrderId(), orderStatusMap.get(evt.getStatus())));
        mailSender.send(msg);
    }

    @PostConstruct
    public void init() {
        orderStatusMap.put(OrderStatus.NEW, "создан");
        orderStatusMap.put(OrderStatus.PAID, "оплачен");
        orderStatusMap.put(OrderStatus.SHIPPED, "отправлен");
        orderStatusMap.put(OrderStatus.COMPLETED, "выполнен");
        orderStatusMap.put(OrderStatus.CANCELLED, "отменён");
    }
}
