package com.github.paicoding.forum.web.shortlink;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.shortlink.ShortLinkDTO;
import com.github.paicoding.forum.api.model.vo.shortlink.ShortLinkReq;
import com.github.paicoding.forum.api.model.vo.shortlink.ShortLinkVO;
import com.github.paicoding.forum.service.shortlink.ShortLinkService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
    public ShortLinkVO createShortLink(@RequestBody ShortLinkReq shortLinkReq) throws NoSuchAlgorithmException {

        String userId = (null == ReqInfoContext.getReqInfo().getUser()) ? "" : ReqInfoContext.getReqInfo().getUser().getUserId().toString();
        ShortLinkDTO shortLinkDTO = new ShortLinkDTO(shortLinkReq.getOriginalUrl(), userId, "");
        return shortLinkService.createShortLink(shortLinkDTO);
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
    public void generateQrCode(@RequestParam String content, HttpServletResponse response) throws IOException, WriterException {
        response.setContentType("image/png");
        // 使用 zxing 生成二维码
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 200, 200);
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", response.getOutputStream());
    }

}
