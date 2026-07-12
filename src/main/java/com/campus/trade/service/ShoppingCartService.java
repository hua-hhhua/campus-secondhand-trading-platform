package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.ShoppingCart;

/**
 * 购物车模块-服务接口层
 * 
 * 定义购物车模块的业务逻辑接口，包括商品加入购物车、购物车列表查询、
 * 商品数量更新、商品删除、清空购物车、批量删除、选中状态切换、
 * 全选/取消全选、结算商品查询、购物车数量统计以及购物车元数据获取等功能。
 * 继承MyBatis-Plus的IService接口，提供基础的CRUD操作。
 */
public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 【购物车模块-加入购物车功能】
     * 
     * 将指定商品添加到用户购物车中。
     * 若商品已在购物车中，则增加商品数量；
     * 若商品不存在，则新增购物车记录。
     * 同时校验库存和用户权限（不能将自己发布的商品加入购物车）。
     * 
     * @param userId    用户ID，标识要操作的购物车所属用户
     * @param articleId 商品ID，标识要加入购物车的商品
     * @param quantity  商品数量，表示要加入购物车的商品件数
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    boolean addToCart(Integer userId, Integer articleId, Integer quantity);

    /**
     * 【购物车模块-购物车列表查询功能】
     * 
     * 分页查询用户购物车中的商品列表。
     * 按创建时间倒序排列，只返回上架中且未售出的商品。
     * 
     * @param userId 用户ID，标识要查询的购物车所属用户
     * @param page   当前页码，表示分页查询的当前页
     * @param size   每页显示数量，表示每页显示的商品记录数
     * @return IPage<ArticleVO> 分页的商品VO列表，包含商品详细信息和分页数据
     */
    IPage<ArticleVO> getCartList(Integer userId, Integer page, Integer size);

    /**
     * 【购物车模块-更新商品数量功能】
     * 
     * 更新购物车中指定商品的购买数量。
     * 根据购物车记录ID找到对应商品，将其数量更新为指定值。
     * 
     * @param cartId   购物车记录ID，标识要更新数量的购物车条目
     * @param quantity 新的商品数量，表示更新后的购买件数
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    boolean updateQuantity(Integer cartId, Integer quantity);

    /**
     * 【购物车模块-删除购物车商品功能】
     * 
     * 从购物车中删除指定的商品记录。
     * 根据购物车记录ID删除对应的购物车条目。
     * 
     * @param cartId 购物车记录ID，标识要删除的购物车条目
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    boolean removeFromCart(Integer cartId);

    /**
     * 【购物车模块-清空购物车功能】
     * 
     * 清空指定用户购物车中的所有商品。
     * 使用事务保证操作的原子性。
     * 
     * @param userId 用户ID，标识要清空购物车的用户
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    boolean clearCart(Integer userId);

    /**
     * 【购物车模块-批量删除购物车商品功能】
     * 
     * 批量删除购物车中指定的多条商品记录。
     * 接收以逗号分隔的购物车记录ID字符串，解析后批量删除。
     * 自动过滤格式不正确的ID。
     * 
     * @param cartIds 购物车记录ID字符串，多个ID之间用逗号分隔，例如："1,2,3"
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    boolean batchRemove(String cartIds);

    /**
     * 【购物车模块-切换单个商品选中状态功能】
     * 
     * 切换购物车中单个商品的选中状态（选中/取消选中）。
     * 选中状态用于标识商品是否参与结算。
     * 
     * @param cartId 购物车记录ID，标识要切换选中状态的购物车条目
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    boolean toggleChecked(Integer cartId);

    /**
     * 【购物车模块-全选/取消全选功能】
     * 
     * 将指定用户购物车中的所有商品设置为选中或取消选中状态。
     * 批量更新所有购物车记录的选中状态。
     * 
     * @param userId  用户ID，标识要操作的购物车所属用户
     * @param checked 选中状态，1表示全选，0表示取消全选
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    boolean toggleAllChecked(Integer userId, Integer checked);

    /**
     * 【购物车模块-查询选中商品功能】
     * 
     * 分页查询用户购物车中已选中的商品列表，用于结算页面展示。
     * 只返回选中状态为已选中的商品，按创建时间倒序排列。
     * 
     * @param userId 用户ID，标识要查询的购物车所属用户
     * @param page   当前页码，表示分页查询的当前页
     * @param size   每页显示数量，表示每页显示的商品记录数
     * @return IPage<ArticleVO> 分页的已选中商品VO列表，包含商品详细信息和分页数据
     */
    IPage<ArticleVO> getCheckedItems(Integer userId, Integer page, Integer size);

    /**
     * 【购物车模块-统计购物车商品数量功能】
     * 
     * 统计用户购物车中的商品总数。
     * 用于在页面导航栏等位置显示购物车商品数量徽章。
     * 
     * @param userId 用户ID，标识要统计的购物车所属用户
     * @return int 购物车中商品的总数量
     */
    int getCartCount(Integer userId);

    /**
     * 【购物车模块-获取购物车元数据映射功能】
     * 
     * 获取用户购物车的元数据映射表，以商品ID为key，购物车信息为value。
     * 购物车信息包含cartId（购物车记录ID）、quantity（商品数量）、checked（选中状态）。
     * 用于在购物车列表页面将购物车元数据填充到商品VO中。
     * 
     * @param userId 用户ID，标识要查询的购物车所属用户
     * @return Map<Integer, Map<String, Object>> 购物车元数据映射表，
     *         key为商品ID，value为包含cartId、quantity、checked的Map
     */
    java.util.Map<Integer, java.util.Map<String, Object>> getCartInfoMap(Integer userId);
}
