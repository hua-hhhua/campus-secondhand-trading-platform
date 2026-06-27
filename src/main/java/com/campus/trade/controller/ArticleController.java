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

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * 我的商品列表（普通用户）/ 商品管理（管理员）
     */
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

        // ========== 根据角色查询不同数据 ==========
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

    /**
     * 商品发布/编辑表单
     */
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
     * 保存商品 - 支持定时发布
     */
    @PostMapping("/save")
    public String saveArticle(
            Article article,
            @RequestParam(required = false) String scheduledTime,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        // ========== 校验用户是否登录 ==========
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "请先登录后再发布商品");
            return "redirect:/toLoginPage";
        }

        // ========== 校验：至少填写一种联系方式 ==========
        boolean hasContact = (article.getWechat() != null && !article.getWechat().trim().isEmpty()) ||
                (article.getQq() != null && !article.getQq().trim().isEmpty()) ||
                (article.getPhone() != null && !article.getPhone().trim().isEmpty());

        if (!hasContact) {
            redirectAttributes.addFlashAttribute("errorMsg", "请至少填写一种联系方式(微信/QQ/手机号)");
            return "redirect:/article/form";
        }

        article.setSendEmail(0);
        article.setUserId(currentUser.getId());

        if (article.getAllowComment() == null) {
            article.setAllowComment(0);
        }

        if (article.getIsTop() == null) {
            article.setIsTop(0);
        }
        if (article.getIsTop() == 2) {
            article.setIsTop(0);
        }
        if (article.getIsTop() != 0 && article.getIsTop() != 1) {
            article.setIsTop(0);
        }

        // ========== 处理定时发布逻辑 ==========
        Integer status = article.getStatus();
        LocalDateTime now = LocalDateTime.now();

        if (status != null && status == ArticleStatus.SCHEDULED) {
            // 定时发布：状态设为2，使用用户选择的时间
            article.setStatus(ArticleStatus.SCHEDULED);

            if (scheduledTime != null && !scheduledTime.isEmpty()) {
                try {
                    LocalDateTime publishTime = LocalDateTime.parse(scheduledTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    article.setPublishedAt(publishTime);
                } catch (Exception e) {
                    // 解析失败，默认1小时后发布
                    article.setPublishedAt(now.plusHours(1));
                }
            } else {
                // 如果没有选择时间，默认1小时后
                article.setPublishedAt(now.plusHours(1));
            }
        } else if (status != null && status == ArticleStatus.DRAFT) {
            // 草稿：状态设为0
            article.setStatus(ArticleStatus.DRAFT);
            article.setPublishedAt(null);
        } else {
            // 立即发布：状态设为1，发布时间为当前时间
            article.setStatus(ArticleStatus.PUBLISHED);
            article.setPublishedAt(now);
        }

        // ========== 保存文章 ==========
        boolean result = articleService.saveOrUpdateArticle(article, currentUser.getId());

        if (result) {
            String statusDesc = "";
            if (article.getStatus() != null) {
                if (article.getStatus() == ArticleStatus.SCHEDULED) {
                    statusDesc = "定时发布（发布时间：" + article.getPublishedAt() + "）";
                } else if (article.getStatus() == ArticleStatus.PUBLISHED) {
                    statusDesc = "发布商品";
                } else {
                    statusDesc = "保存草稿";
                }
            }
            asyncService.asyncLogOperation(currentUser.getUsername(), statusDesc, "商品标题: " + article.getTitle());

            if (article.getStatus() != null && article.getStatus() == ArticleStatus.PUBLISHED) {
                asyncService.asyncUpdateArticleStats(article.getId(), "publish");
            }
        }

        return "redirect:/article/manage";
    }

    /**
     * 删除商品
     */
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

    /**
     * 下架商品
     */
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
        }
        return "redirect:/article/manage";
    }

    /**
     * 上架商品
     */
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
        }
        return "redirect:/article/manage";
    }

    /**
     * 上传商品图片
     */
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