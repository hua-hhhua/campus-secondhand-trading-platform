package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("order_status_history")
public class OrderStatusHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Integer fromStatus;
    private Integer toStatus;
    private String remark;
    private LocalDateTime createdAt;
}
