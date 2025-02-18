package com.github.paicoding.forum.web.front.shortlink.rest;

import com.github.hui.quick.plugin.base.awt.ImageLoadUtil;
import com.github.hui.quick.plugin.qrcode.v3.entity.QrResource;
import com.github.hui.quick.plugin.qrcode.wrapper.QrCodeGenV3;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.shortlink.ShortLinkReq;
import com.github.paicoding.forum.api.model.vo.shortlink.ShortLinkVO;
import com.github.paicoding.forum.api.model.vo.shortlink.dto.ShortLinkDTO;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.shortlink.service.ShortLinkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/sol")
public class ShortLinkController {

    @Resource
    private ShortLinkService shortLinkService;

    /**
     * 创建短链接
     *
     * @param shortLinkReq 包含原始长链接
     * @return 创建的短链接信息
     */
    @PostMapping("/url")
    public ResVo<ShortLinkVO> createShortLink(@RequestBody ShortLinkReq shortLinkReq) throws NoSuchAlgorithmException {
        String userId = (null == ReqInfoContext.getReqInfo().getUser()) ? "" : ReqInfoContext.getReqInfo().getUser().getUserId().toString();
        ShortLinkDTO shortLinkDTO = new ShortLinkDTO(shortLinkReq.getOriginalUrl(), userId, "");
        return ResVo.ok(shortLinkService.createShortLink(shortLinkDTO));
    }

    /**
     * 根据短链接获取原始长链接
     *
     * @param shortCode 短链接
     */
    @GetMapping("/{shortCode}")
    public void getOriginalLink(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        ShortLinkVO shortLinkVO = shortLinkService.getOriginalLink(shortCode);
        response.sendRedirect(shortLinkVO.getOriginalUrl());
    }

    @GetMapping("/gen")
    public void generateQrCode(@RequestParam String content, @RequestParam(required = false) Integer size, HttpServletResponse response) throws Exception {
        BufferedImage img = QrCodeGenV3.of(content)
                .setSize(size == null || size < 200 ? 200 : Math.min(size, 500))
                .setLogo(getDefaultLogo())
                .asImg();
        response.setContentType("image/png");
        ImageIO.write(img, "png", response.getOutputStream());
    }

    private QrResource getDefaultLogo() {
        BufferedImage img;
        try {
            img = ImageLoadUtil.getImageByPath(SpringUtil.getConfigOrElse("view.site.websiteFaviconIconUrl", "https://paicoding.com/img/icon.png"));
        } catch (Exception e) {
            return null;
        }
        return new QrResource().setImg(img);
    }
}
