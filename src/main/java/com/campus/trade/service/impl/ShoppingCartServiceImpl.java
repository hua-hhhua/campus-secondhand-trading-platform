package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.ShoppingCart;
import com.campus.trade.mapper.ArticleMapper;
import com.campus.trade.mapper.ShoppingCartMapper;
import com.campus.trade.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车模块-服务实现层
 * 
 * 购物车模块业务逻辑的具体实现类，实现ShoppingCartService接口中定义的所有方法。
 * 继承MyBatis-Plus的ServiceImpl，提供基础的CRUD操作实现。
 * 包含商品加入购物车、购物车列表查询、商品数量更新、商品删除、
 * 清空购物车、批量删除、选中状态切换、全选/取消全选、
 * 结算商品查询、购物车数量统计以及购物车元数据获取等核心业务逻辑。
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
        implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 【购物车模块-加入购物车功能】
     * 
     * 将指定商品添加到用户购物车中。
     * 业务逻辑：
     * 1. 校验数量是否合法（必须大于0）
     * 2. 查询商品信息，校验商品是否存在及库存是否有效
     * 3. 校验用户不能将自己发布的商品加入购物车
     * 4. 查询该商品是否已在购物车中：
     * - 若已存在，则增加数量并校验总数量不超过库存
     * - 若不存在，则新增购物车记录并校验数量不超过库存
     * 5. 新增记录默认设置为选中状态
     * 
     * @param userId    用户ID，标识要操作的购物车所属用户
     * @param articleId 商品ID，标识要加入购物车的商品
     * @param quantity  商品数量，表示要加入购物车的商品件数
     * @return boolean 操作是否成功，true表示成功，false表示失败
     * @throws RuntimeException 当用户将自己发布的商品加入购物车或购买数量超过库存时抛出
     */
    @Override
    public boolean addToCart(Integer userId, Integer articleId, Integer quantity) {
        if (quantity <= 0) {
            return false;
        }

        // 查询商品信息，校验库存
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getStock() == null) {
            return false;
        }

        // 不能将自己发布的商品加入购物车
        if (article.getUserId().equals(userId)) {
            throw new RuntimeException("不能将自己发布的商品加入购物车");
        }

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getArticleId, articleId);

        ShoppingCart existing = this.getOne(wrapper);

        if (existing != null) {
            int newQuantity = existing.getQuantity() + quantity;
            if (newQuantity > article.getStock()) {
                throw new RuntimeException("购买数量不能超过库存数量（" + article.getStock() + "）");
            }
            existing.setQuantity(newQuantity);
            return this.updateById(existing);
        } else {
            if (quantity > article.getStock()) {
                throw new RuntimeException("购买数量不能超过库存数量（" + article.getStock() + "）");
            }
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setArticleId(articleId);
            cart.setQuantity(quantity);
            cart.setChecked(1);
            return this.save(cart);
        }
    }

    /**
     * 【购物车模块-购物车列表查询功能】
     * 
     * 分页查询用户购物车中的商品列表。
     * 业务逻辑：
     * 1. 查询用户所有购物车记录，按创建时间倒序排列
     * 2. 若购物车为空，直接返回空分页结果
     * 3. 提取购物车中的商品ID列表
     * 4. 根据商品ID列表查询商品详情（只返回上架中且未售出的商品）
     * 5. 返回分页的商品VO列表
     * 
     * @param userId 用户ID，标识要查询的购物车所属用户
     * @param page   当前页码，表示分页查询的当前页
     * @param size   每页显示数量，表示每页显示的商品记录数
     * @return IPage<ArticleVO> 分页的商品VO列表，包含商品详细信息和分页数据
     */
    @Override
    public IPage<ArticleVO> getCartList(Integer userId, Integer page, Integer size) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> carts = this.list(wrapper);

        Page<ArticleVO> pageParam = new Page<>(page, size);

        if (carts.isEmpty()) {
            pageParam.setTotal(0);
            pageParam.setRecords(new ArrayList<>());
            return pageParam;
        }

        List<Integer> articleIds = carts.stream()
                .map(ShoppingCart::getArticleId)
                .collect(Collectors.toList());

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Article> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

        queryWrapper.in("a.id", articleIds);
        queryWrapper.eq("a.status", 1);
        queryWrapper.eq("a.product_status", 0);

        Page<ArticleVO> articleVOPage = new Page<>(page, size);
        IPage<ArticleVO> result = articleMapper.selectArticleVOPage(articleVOPage, queryWrapper);

        return result;
    }

    /**
     * 【购物车模块-更新商品数量功能】
     * 
     * 更新购物车中指定商品的购买数量。
     * 业务逻辑：
     * 1. 校验数量是否合法（必须大于0）
     * 2. 根据购物车记录ID查询购物车记录
     * 3. 若记录存在，更新商品数量并保存
     * 
     * @param cartId   购物车记录ID，标识要更新数量的购物车条目
     * @param quantity 新的商品数量，表示更新后的购买件数
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    @Override
    public boolean updateQuantity(Integer cartId, Integer quantity) {
        if (quantity <= 0) {
            return false;
        }
        ShoppingCart cart = this.getById(cartId);
        if (cart != null) {
            cart.setQuantity(quantity);
            return this.updateById(cart);
        }
        return false;
    }

    /**
     * 【购物车模块-删除购物车商品功能】
     * 
     * 从购物车中删除指定的商品记录。
     * 根据购物车记录ID直接删除对应的购物车条目。
     * 
     * @param cartId 购物车记录ID，标识要删除的购物车条目
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    @Override
    public boolean removeFromCart(Integer cartId) {
        return this.removeById(cartId);
    }

    /**
     * 【购物车模块-清空购物车功能】
     * 
     * 清空指定用户购物车中的所有商品。
     * 使用事务注解保证操作的原子性，即要么全部删除成功，要么全部不删除。
     * 
     * @param userId 用户ID，标识要清空购物车的用户
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    @Override
    @Transactional
    public boolean clearCart(Integer userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        return this.remove(wrapper);
    }

    /**
     * 【购物车模块-批量删除购物车商品功能】
     * 
     * 批量删除购物车中指定的多条商品记录。
     * 业务逻辑：
     * 1. 校验ID字符串是否为空
     * 2. 按逗号分割ID字符串
     * 3. 解析每个ID为整数，自动过滤格式不正确的ID
     * 4. 若解析后的ID列表为空，返回失败
     * 5. 批量删除指定ID的购物车记录
     * 
     * @param cartIds 购物车记录ID字符串，多个ID之间用逗号分隔，例如："1,2,3"
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    @Override
    public boolean batchRemove(String cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            return false;
        }
        String[] ids = cartIds.split(",");
        List<Integer> idList = new ArrayList<>();
        for (String id : ids) {
            try {
                idList.add(Integer.parseInt(id.trim()));
            } catch (NumberFormatException e) {
                continue;
            }
        }
        if (idList.isEmpty()) {
            return false;
        }
        return this.removeByIds(idList);
    }

    /**
     * 【购物车模块-切换单个商品选中状态功能】
     * 
     * 切换购物车中单个商品的选中状态（选中/取消选中）。
     * 业务逻辑：
     * 1. 根据购物车记录ID查询购物车记录
     * 2. 若记录存在，切换选中状态（1变0，0变1）
     * 3. 更新并保存记录
     * 
     * @param cartId 购物车记录ID，标识要切换选中状态的购物车条目
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    @Override
    public boolean toggleChecked(Integer cartId) {
        ShoppingCart cart = this.getById(cartId);
        if (cart != null) {
            cart.setChecked(cart.getChecked() == 1 ? 0 : 1);
            return this.updateById(cart);
        }
        return false;
    }

    /**
     * 【购物车模块-全选/取消全选功能】
     * 
     * 将指定用户购物车中的所有商品设置为选中或取消选中状态。
     * 业务逻辑：
     * 1. 查询用户所有购物车记录
     * 2. 若购物车为空，返回失败
     * 3. 遍历所有购物车记录，设置选中状态
     * 4. 批量更新所有记录
     * 
     * @param userId  用户ID，标识要操作的购物车所属用户
     * @param checked 选中状态，1表示全选，0表示取消全选
     * @return boolean 操作是否成功，true表示成功，false表示失败
     */
    @Override
    public boolean toggleAllChecked(Integer userId, Integer checked) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);

        List<ShoppingCart> carts = this.list(wrapper);
        if (carts.isEmpty()) {
            return false;
        }

        for (ShoppingCart cart : carts) {
            cart.setChecked(checked);
        }

        return this.updateBatchById(carts);
    }

    /**
     * 【购物车模块-查询选中商品功能】
     * 
     * 分页查询用户购物车中已选中的商品列表，用于结算页面展示。
     * 业务逻辑：
     * 1. 查询用户所有已选中的购物车记录，按创建时间倒序排列
     * 2. 若没有选中的商品，直接返回空分页结果
     * 3. 提取选中商品的ID列表
     * 4. 根据商品ID列表查询商品详情（只返回上架中且未售出的商品）
     * 5. 返回分页的商品VO列表
     * 
     * @param userId 用户ID，标识要查询的购物车所属用户
     * @param page   当前页码，表示分页查询的当前页
     * @param size   每页显示数量，表示每页显示的商品记录数
     * @return IPage<ArticleVO> 分页的已选中商品VO列表，包含商品详细信息和分页数据
     */
    @Override
    public IPage<ArticleVO> getCheckedItems(Integer userId, Integer page, Integer size) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getChecked, 1)
                .orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> carts = this.list(wrapper);

        Page<ArticleVO> pageParam = new Page<>(page, size);

        if (carts.isEmpty()) {
            pageParam.setTotal(0);
            pageParam.setRecords(new ArrayList<>());
            return pageParam;
        }

        List<Integer> articleIds = carts.stream()
                .map(ShoppingCart::getArticleId)
                .collect(Collectors.toList());

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Article> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

        queryWrapper.in("a.id", articleIds);
        queryWrapper.eq("a.status", 1);
        queryWrapper.eq("a.product_status", 0);

        Page<ArticleVO> articleVOPage = new Page<>(page, size);
        IPage<ArticleVO> result = articleMapper.selectArticleVOPage(articleVOPage, queryWrapper);

        return result;
    }

    /**
     * 【购物车模块-统计购物车商品数量功能】
     * 
     * 统计用户购物车中的商品总数。
     * 直接通过Mapper层的自定义SQL统计用户购物车中的商品记录数。
     * 
     * @param userId 用户ID，标识要统计的购物车所属用户
     * @return int 购物车中商品的总数量
     */
    @Override
    public int getCartCount(Integer userId) {
        return shoppingCartMapper.countByUserId(userId);
    }

    /**
     * 【购物车模块-获取购物车元数据映射功能】
     * 
     * 获取用户购物车的元数据映射表，以商品ID为key，购物车信息为value。
     * 业务逻辑：
     * 1. 查询用户所有购物车记录
     * 2. 遍历购物车记录，构建以商品ID为key的Map
     * 3. 每个value包含cartId（购物车记录ID）、quantity（商品数量）、checked（选中状态）
     * 
     * @param userId 用户ID，标识要查询的购物车所属用户
     * @return Map<Integer, Map<String, Object>> 购物车元数据映射表，
     *         key为商品ID，value为包含cartId、quantity、checked的Map
     */
    @Override
    public java.util.Map<Integer, java.util.Map<String, Object>> getCartInfoMap(Integer userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);

        List<ShoppingCart> carts = this.list(wrapper);

        java.util.Map<Integer, java.util.Map<String, Object>> map = new java.util.HashMap<>();
        for (ShoppingCart cart : carts) {
            java.util.Map<String, Object> info = new java.util.HashMap<>();
            info.put("cartId", cart.getId());
            info.put("quantity", cart.getQuantity());
            info.put("checked", cart.getChecked());
            map.put(cart.getArticleId(), info);
        }

        return map;
    }
}
