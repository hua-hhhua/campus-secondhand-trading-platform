package com.campus.trade.service.impl;

import com.campus.trade.entity.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusValidator {

    public boolean isValidStatusTransition(int fromStatus, int toStatus) {
        return isValidStatusTransition(OrderStatus.fromCode(fromStatus), OrderStatus.fromCode(toStatus));
    }

    public boolean isValidStatusTransition(OrderStatus fromStatus, OrderStatus toStatus) {
        switch (fromStatus) {
            case PENDING_PAYMENT:
                return toStatus == OrderStatus.PENDING_SHIP || toStatus == OrderStatus.CANCELLED;
            case PENDING_SHIP:
                return toStatus == OrderStatus.SHIPPED || toStatus == OrderStatus.CANCELLED;
            case SHIPPED:
                return toStatus == OrderStatus.COMPLETED || toStatus == OrderStatus.CANCELLED;
            case COMPLETED:
                return toStatus == OrderStatus.REFUNDED;
            default:
                return false;
        }
    }

    public boolean canCancel(int status) {
        return status == OrderStatus.PENDING_PAYMENT.getCode() ||
                status == OrderStatus.PENDING_SHIP.getCode();
    }

    public boolean canConfirm(int status) {
        return status == OrderStatus.SHIPPED.getCode();
    }

    public boolean canReview(int status) {
        return status == OrderStatus.COMPLETED.getCode();
    }
}
