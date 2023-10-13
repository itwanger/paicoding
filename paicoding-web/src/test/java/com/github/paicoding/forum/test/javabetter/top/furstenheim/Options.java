package com.github.paicoding.forum.test.javabetter.top.furstenheim;

public class Options {
    final String br;
    final String hr;
    final String emDelimiter;
    final String strongDelimiter;
    final HeadingStyle headingStyle;
    final String bulletListMaker;
    final CodeBlockStyle codeBlockStyle;
    final LinkStyle linkStyle;
    final LinkReferenceStyle linkReferenceStyle;
    final String fence;

    public Options(String br, String hr, String emDelimiter, String strongDelimiter,
            HeadingStyle headingStyle, String bulletListMaker, CodeBlockStyle codeBlockStyle,
            LinkStyle linkStyle, LinkReferenceStyle linkReferenceStyle, String fence) {
        this.br = br;
        this.hr = hr;
        this.emDelimiter = emDelimiter;
        this.strongDelimiter = strongDelimiter;
        this.headingStyle = headingStyle;
        this.bulletListMaker = bulletListMaker;
        this.codeBlockStyle = codeBlockStyle;
        this.linkStyle = linkStyle;
        this.linkReferenceStyle = linkReferenceStyle;
        this.fence = fence;
    }
}
