package com.github.paicoding.forum.api.model.event;

import com.github.paicoding.forum.api.model.enums.ArticleEventEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ArticleMsgEvent<T> extends ApplicationEvent {

    private ArticleEventEnum type;

    private T content;


    public ArticleMsgEvent(Object source, ArticleEventEnum type, T content) {
        super(source);
        this.type = type;
        this.content = content;
    }
}
