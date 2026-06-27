package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.constant.ArticleStatus;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.Category;
import com.campus.trade.entity.School;
import com.campus.trade.entity.Tag;
import com.campus.trade.entity.User;
import com.campus.trade.service.ArticleService;
import com.campus.trade.service.AsyncService;
import com.campus.trade.service.CategoryService;
import com.campus.trade.service.SchoolService;
import com.campus.trade.service.TagService;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private TagService tagService;

    @GetMapping("/manage")
    public String articlesEntry(
            Model model,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer statusFilter,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/toLoginPage";
        }

        IPage<ArticleVO> articlePage;

        if (currentUser.getRole() == 1) {
            articlePage = articleService.getArticleVOPage(page, size, keyword, statusFilter, startTime, endTime);
        } else {
            articlePage = articleService.getArticleVOPageByUserId(page, size, keyword, statusFilter, startTime, endTime, currentUser.getId());
        }

        model.addAttribute("articlePage", articlePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);

        if (currentUser.getRole() == 1) {
            model.addAttribute("isAdmin", true);
            return "admin/article-manage";
        } else {
            return "admin/my-articles";
        }
    }

    @GetMapping("/form")
    public String articleForm(@RequestParam(required = false) Integer id, Model model, HttpSession session) {
        List<Category> categories = categoryService.list();
        model.addAttribute("categories", categories);

        List<School> schools = schoolService.list();
        model.addAttribute("schools", schools);

        List<Tag> tags = tagService.list();
        model.addAttribute("tags", tags);

        if (id != null) {
            Article article = articleService.getById(id);
            if (article.getSendEmail() == null) {
                article.setSendEmail(0);
            }
            if (article.getAllowComment() == null) {
                article.setAllowComment(1);
            }
            if (article.getIsTop() == null) {
                article.setIsTop(0);
            }
            List<Integer> tagIds = articleService.getTagIdsByArticleId(id);
            article.setTagIds(tagIds);
            model.addAttribute("article", article);
        } else {
            model.addAttribute("article", new Article());
        }
        model.addAttribute("statusDraft", ArticleStatus.DRAFT);
        model.addAttribute("statusPublished", ArticleStatus.PUBLISHED);
        model.addAttribute("statusScheduled", ArticleStatus.SCHEDULED);
        return "admin/article-form";
    }

    /**
     * 保存商品 - 支持定时发布和草稿保存
     */
    @PostMapping("/save")
    public String saveArticle(
            HttpServletRequest request,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String scheduledTime,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) Integer stock,
            @RequestParam(required = false) Integer schoolId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String coverImage,
            @RequestParam(required = false) String wechat,
            @RequestParam(required = false) String qq,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer isTop,
            @RequestParam(required = false) Integer allowComment,
            @RequestParam(required = false) List<Integer> tagIds,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ========== 打印所有请求参数（调试用） ==========
        System.out.println("========================================");
        System.out.println("========== 所有请求参数 ==========");
        Map<String, String[]> paramMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            System.out.println("参数名: " + key + ", 值: " + String.join(", ", values));
        }
        System.out.println("========== 打印结束 ==========");

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "请先登录后再发布商品");
            return "redirect:/toLoginPage";
        }

        // ========== 校验联系方式 ==========
        boolean hasContact = (wechat != null && !wechat.trim().isEmpty()) ||
                (qq != null && !qq.trim().isEmpty()) ||
                (phone != null && !phone.trim().isEmpty());

        if (!hasContact) {
            redirectAttributes.addFlashAttribute("errorMsg", "请至少填写一种联系方式(微信/QQ/手机号)");
            return "redirect:/article/form";
        }

        // ========== 创建或获取 Article 对象 ==========
        Article article;
        if (id != null) {
            article = articleService.getById(id);
            if (article == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "商品不存在");
                return "redirect:/article/manage";
            }
        } else {
            article = new Article();
            article.setViewCount(0);
            article.setCreateTime(LocalDateTime.now());
        }

        // ========== 设置基本字段 ==========
        article.setTitle(title);
        article.setContent(content);
        article.setCategoryId(categoryId);
        article.setUserId(currentUser.getId());
        article.setPrice(price != null ? price : BigDecimal.ZERO);
        article.setStock(stock != null ? stock : 1);
        article.setSchoolId(schoolId);
        article.setLocation(location);
        article.setCoverImage(coverImage);
        article.setWechat(wechat);
        article.setQq(qq);
        article.setPhone(phone);
        article.setSendEmail(0);
        article.setAllowComment(allowComment != null ? allowComment : 1);
        article.setIsTop(isTop != null && isTop == 1 ? 1 : 0);
        article.setUpdateTime(LocalDateTime.now());
        article.setTagIds(tagIds);

        // ========== 处理发布状态 ==========
        LocalDateTime now = LocalDateTime.now();

        System.out.println("========== 接收到的 status 值: " + status);

        // 如果 status 为 null，默认为立即发布
        if (status == null) {
            status = 1;
        }

        // 直接设置 status，不经过任何其他逻辑
        article.setStatus(status);
        System.out.println("========== 设置 article.status = " + article.getStatus());

        // ========== 根据发布状态设置商品状态 ==========
        if (status == 0) {
            // 草稿：设置为已下架，不在首页显示
            article.setProductStatus(2);
            article.setPublishedAt(null);
            System.out.println("========== 保存为草稿，商品状态设为已下架");
        } else if (status == 2) {
            // 定时发布：先设置为已下架，等定时任务触发时再上架
            article.setProductStatus(2);
            if (scheduledTime != null && !scheduledTime.isEmpty()) {
                try {
                    article.setPublishedAt(LocalDateTime.parse(scheduledTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } catch (Exception e) {
                    article.setPublishedAt(now.plusHours(1));
                }
            } else {
                article.setPublishedAt(now.plusHours(1));
            }
            System.out.println("========== 保存为定时发布，发布时间: " + article.getPublishedAt() + "，商品状态设为已下架");
        } else {
            // 立即发布：设置为在售
            article.setProductStatus(0);
            article.setPublishedAt(now);
            System.out.println("========== 保存为立即发布，商品状态设为在售");
        }

        // ========== 保存文章 ==========
        boolean result = articleService.saveOrUpdateArticle(article, currentUser.getId());

        if (result) {
            // 保存标签关联
            if (tagIds != null && !tagIds.isEmpty()) {
                articleService.saveArticleTags(article.getId(), tagIds);
            }

            String statusDesc = "";
            if (article.getStatus() != null) {
                if (article.getStatus() == 2) {
                    statusDesc = "定时发布（发布时间：" + article.getPublishedAt() + "）";
                } else if (article.getStatus() == 1) {
                    statusDesc = "发布商品";
                } else {
                    statusDesc = "保存草稿";
                }
            }
            asyncService.asyncLogOperation(currentUser.getUsername(), statusDesc, "商品标题: " + article.getTitle());

            if (article.getStatus() != null && article.getStatus() == 1) {
                asyncService.asyncUpdateArticleStats(article.getId(), "publish");
            }
        }

        return "redirect:/article/manage";
    }

    @GetMapping("/delete/{id}")
    public String deleteArticle(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/toLoginPage";
        }

        Article article = articleService.getById(id);

        if (article != null && (currentUser.getRole() == 1 || article.getUserId().equals(currentUser.getId()))) {
            String articleTitle = article.getTitle();
            articleService.deleteArticle(id);
            asyncService.asyncLogOperation(currentUser.getUsername(), "删除商品", "商品ID: " + id + ", 标题: " + articleTitle);
        }
        return "redirect:/article/manage";
    }

    @GetMapping("/off-shelf/{id}")
    public String offShelf(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/toLoginPage";
        }

        Article article = articleService.getById(id);

        if (article != null && (article.getUserId().equals(currentUser.getId()) || currentUser.getRole() == 1)) {
            article.setProductStatus(2);
            articleService.updateById(article);
            asyncService.asyncLogOperation(currentUser.getUsername(), "下架商品", "商品ID: " + id + ", 标题: " + article.getTitle());
        }
        return "redirect:/article/manage";
    }

    @GetMapping("/on-shelf/{id}")
    public String onShelf(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/toLoginPage";
        }

        Article article = articleService.getById(id);

        if (article != null && (article.getUserId().equals(currentUser.getId()) || currentUser.getRole() == 1)) {
            article.setProductStatus(0);
            articleService.updateById(article);
            asyncService.asyncLogOperation(currentUser.getUsername(), "上架商品", "商品ID: " + id + ", 标题: " + article.getTitle());
        }
        return "redirect:/article/manage";
    }

    @PostMapping("/upload-image")
    @ResponseBody
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要上传的图片");
                return result;
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "只能上传图片文件");
                return result;
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "图片大小不能超过 5MB");
                return result;
            }

            User currentUser = userService.getUserByUsername(
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName()
            );

            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            String imagePath = articleService.uploadCoverImage(file, currentUser.getId());
            result.put("success", true);
            result.put("imagePath", imagePath);
            result.put("message", "上传成功");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上传失败：" + e.getMessage());
        }
        return result;
    }
}