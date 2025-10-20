package com.github.paicoding.forum.core.markdown;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Extension for rendering images with captions from alt text
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * <p>
 * This extension automatically wraps images with alt text in figure tags
 * and displays the alt text as a caption below the image.
 *
 * @author 沉默王二
 * @date 2025-10-20
 */
public class ImageCaptionExtension implements HtmlRenderer.HtmlRendererExtension {

    private ImageCaptionExtension() {
    }

    public static ImageCaptionExtension create() {
        return new ImageCaptionExtension();
    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {
        // No options needed for now
    }

    @Override
    public void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new ImageCaptionNodeRenderer.Factory());
        }
    }
}
