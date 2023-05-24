package com.github.paicoding.forum.test.javabetter.top.furstenheim;

public final class OptionsBuilder {
    private String br = "  ";
    private String hr = "* * *";
    private String emDelimiter = "_";
    private String strongDelimiter = "**";
    private HeadingStyle headingStyle = HeadingStyle.SETEXT;
    private String bulletListMaker = "*";
    private CodeBlockStyle codeBlockStyle = CodeBlockStyle.INDENTED;
    private LinkStyle linkStyle = LinkStyle.INLINED;
    private LinkReferenceStyle linkReferenceStyle = LinkReferenceStyle.DEFAULT;
    public String fence = "```";

    private OptionsBuilder() {
    }

    public static OptionsBuilder anOptions() {
        return new OptionsBuilder();
    }

    public OptionsBuilder withBr(String br) {
        this.br = br;
        return this;
    }

    public OptionsBuilder withHr(String hr) {
        this.hr = hr;
        return this;
    }

    public OptionsBuilder withEmDelimiter(String emDelimiter) {
        this.emDelimiter = emDelimiter;
        return this;
    }

    public OptionsBuilder withStrongDelimiter(String strongDelimiter) {
        this.strongDelimiter = strongDelimiter;
        return this;
    }

    public OptionsBuilder withHeadingStyle(HeadingStyle headingStyle) {
        this.headingStyle = headingStyle;
        return this;
    }

    public OptionsBuilder withBulletListMaker(String bulletListMaker) {
        this.bulletListMaker = bulletListMaker;
        return this;
    }

    public OptionsBuilder withCodeBlockStyle(CodeBlockStyle codeBlockStyle) {
        this.codeBlockStyle = codeBlockStyle;
        return this;
    }

    public OptionsBuilder withLinkStyle(LinkStyle linkStyle) {
        this.linkStyle = linkStyle;
        return this;
    }

    public OptionsBuilder withLinkReferenceStyle(LinkReferenceStyle linkReferenceStyle) {
        this.linkReferenceStyle = linkReferenceStyle;
        return this;
    }

    public OptionsBuilder withFence(String fence) {
        this.fence = fence;
        return this;
    }

    public Options build() {
        return new Options(br, hr, emDelimiter, strongDelimiter, headingStyle, bulletListMaker, codeBlockStyle,
                           linkStyle, linkReferenceStyle, fence);
    }
}
