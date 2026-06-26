package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.OrderStatusHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface OrderStatusHistoryMapper extends BaseMapper<OrderStatusHistory> {
    @Insert("INSERT INTO order_status_history (order_id, from_status, to_status, remark, created_at) " +
            "VALUES (#{orderId}, #{fromStatus}, #{toStatus}, #{remark}, #{createdAt})")
    int insertHistory(@Param("orderId") Long orderId,
                      @Param("fromStatus") Integer fromStatus,
                      @Param("toStatus") Integer toStatus,
                      @Param("remark") String remark,
                      @Param("createdAt") LocalDateTime createdAt);
}
