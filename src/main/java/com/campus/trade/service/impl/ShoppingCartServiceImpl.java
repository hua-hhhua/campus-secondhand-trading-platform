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

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private ArticleMapper articleMapper;

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
        
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Article> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        queryWrapper.in("a.id", articleIds);
        queryWrapper.eq("a.status", 1);
        queryWrapper.eq("a.product_status", 0);
        
        Page<ArticleVO> articleVOPage = new Page<>(page, size);
        IPage<ArticleVO> result = articleMapper.selectArticleVOPage(articleVOPage, queryWrapper);
        
        return result;
    }

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

    @Override
    public boolean removeFromCart(Integer cartId) {
        return this.removeById(cartId);
    }

    @Override
    @Transactional
    public boolean clearCart(Integer userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        return this.remove(wrapper);
    }

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

    @Override
    public boolean toggleChecked(Integer cartId) {
        ShoppingCart cart = this.getById(cartId);
        if (cart != null) {
            cart.setChecked(cart.getChecked() == 1 ? 0 : 1);
            return this.updateById(cart);
        }
        return false;
    }

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
        
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Article> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        queryWrapper.in("a.id", articleIds);
        queryWrapper.eq("a.status", 1);
        queryWrapper.eq("a.product_status", 0);
        
        Page<ArticleVO> articleVOPage = new Page<>(page, size);
        IPage<ArticleVO> result = articleMapper.selectArticleVOPage(articleVOPage, queryWrapper);
        
        return result;
    }

    @Override
    public int getCartCount(Integer userId) {
        return shoppingCartMapper.countByUserId(userId);
    }

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
