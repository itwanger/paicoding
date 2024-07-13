package com.github.paicoding.forum.api.model.vo.notify;

import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
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
public class NotifyMsgEvent<T> extends ApplicationEvent {

    private NotifyTypeEnum notifyType;

    private T content;


    public NotifyMsgEvent(Object source, NotifyTypeEnum notifyType, T content) {
        super(source);
        this.notifyType = notifyType;
        this.content = content;
    }


}
