package com.ecommerce.application.enums;

public enum OrderStatus {
    ORDER_PLACED,
    CANCELLED,
    ORDER_CONFIRMED,
    ORDER_REJECTED,
    ORDER_SHIPPED,
    DELIVERED,
    RETURN_REQUESTED,
    RETURN_REJECTED,
    RETURN_APPROVED,
    PICK_UP_INITIATED,
    PICK_UP_COMPLETED,
    REFUND_INITIATED,
    REFUND_COMPLETED,
    CLOSED;

    public boolean isValid(OrderStatus next) {
        return switch (this) {
            case ORDER_PLACED -> next == CANCELLED || next == ORDER_CONFIRMED || next == ORDER_REJECTED;
            case CANCELLED, ORDER_REJECTED -> next == REFUND_INITIATED || next == CLOSED;
            case ORDER_CONFIRMED -> next == CANCELLED || next == ORDER_SHIPPED;
            case ORDER_SHIPPED -> next == DELIVERED;
            case DELIVERED -> next == RETURN_REQUESTED || next == CLOSED;
            case RETURN_REQUESTED -> next == RETURN_REJECTED || next == RETURN_APPROVED;
            case RETURN_REJECTED, REFUND_COMPLETED -> next == CLOSED;
            case RETURN_APPROVED -> next == PICK_UP_INITIATED;
            case PICK_UP_INITIATED -> next == PICK_UP_COMPLETED;
            case PICK_UP_COMPLETED -> next == REFUND_INITIATED;
            case REFUND_INITIATED -> next == REFUND_COMPLETED;
            case CLOSED -> false;
        };
    }
}
