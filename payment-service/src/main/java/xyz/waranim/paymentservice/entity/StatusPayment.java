package xyz.waranim.paymentservice.entity;

public enum StatusPayment {
    NEW,
    WAITING_FOR_CAPTURE,
    SUCCEEDED,
    CANCELED
}
