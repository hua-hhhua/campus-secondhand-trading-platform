package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_items")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Integer articleId;
    private String articleTitle;
    private String articleCover;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;
    private LocalDateTime createdAt;
}
