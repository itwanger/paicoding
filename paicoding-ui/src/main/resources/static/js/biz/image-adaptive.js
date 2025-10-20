/**
 * 图片自适应宽度
 * 根据图片原始宽度智能调整显示宽度：
 * - 原始宽度 >= 1000px：允许充满容器（最大 100%）
 * - 原始宽度 < 1000px：限制最大宽度为 500px
 */
(function() {
    'use strict';

    function adjustImageWidth() {
        // 获取所有 figure 中的图片
        const images = document.querySelectorAll('.article-content img');

        images.forEach(function(img) {
            // 如果图片已经加载完成
            if (img.complete && img.naturalWidth > 0) {
                applyWidthLimit(img);
            } else {
                // 等待图片加载完成
                img.addEventListener('load', function() {
                    applyWidthLimit(img);
                });
            }
        });
    }

    function applyWidthLimit(img) {
        const naturalWidth = img.naturalWidth;
        const isMobile = window.innerWidth <= 768;

        if (naturalWidth >= 1200) {
            // 大图：允许充满容器
            img.style.maxWidth = '100%';
        } else {
            // 小图：在移动端不限制宽度，在PC端限制最大宽度为 500px
            img.style.maxWidth = isMobile ? '100%' : '500px';
        }
    }

    // DOM 加载完成后执行
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', adjustImageWidth);
    } else {
        adjustImageWidth();
    }

    // 如果有动态加载的内容，可以暴露方法供外部调用
    window.adjustImageWidth = adjustImageWidth;
})();
