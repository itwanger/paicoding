package com.github.paicoding.forum.core.markdown;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Node renderer for images with captions
 * <p>
 * Renders images with alt text wrapped in figure tags with figcaption
 *
 * @author 沉默王二
 * @date 2025-10-20
 */
public class ImageCaptionNodeRenderer implements NodeRenderer {

    public ImageCaptionNodeRenderer(DataHolder options) {
        // No options needed for now
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(Image.class, this::render));
        return set;
    }

    private void render(Image node, NodeRendererContext context, HtmlWriter html) {
        if (!context.isDoNotRenderLinks()) {
            // Collect the alt text from the image node
            String altText = new TextCollectingVisitor().collectAndGetText(node);

            // Resolve the image URL
            ResolvedLink resolvedLink = context.resolveLink(LinkType.IMAGE, node.getUrl().unescape(), null);
            String url = resolvedLink.getUrl();

            // Check if alt text exists and is not empty
            if (altText != null && !altText.trim().isEmpty()) {
                // Render as figure with caption
                html.line();
                html.withAttr().tag("figure");
                html.line();

                // Render the image
                html.attr("src", url);
                html.attr("alt", altText);
                if (node.getTitle().isNotNull()) {
                    html.attr("title", node.getTitle().unescape());
                }
                html.srcPos(node.getChars()).withAttr(resolvedLink).tagVoid("img");
                html.line();

                // Render the caption
                html.withAttr().tag("figcaption");
                html.text(altText);
                html.tag("/figcaption");
                html.line();

                html.tag("/figure");
                html.line();
            } else {
                // No alt text, render as normal image
                html.attr("src", url);
                if (node.getTitle().isNotNull()) {
                    html.attr("title", node.getTitle().unescape());
                }
                html.srcPos(node.getChars()).withAttr(resolvedLink).tagVoidLine("img");
            }
        }
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new ImageCaptionNodeRenderer(options);
        }
    }
}
