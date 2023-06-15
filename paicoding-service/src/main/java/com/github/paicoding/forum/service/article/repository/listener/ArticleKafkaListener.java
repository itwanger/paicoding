package com.github.paicoding.forum.service.article.repository.listener;

import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.github.paicoding.forum.api.model.constant.KafkaTopicConstant;
import com.github.paicoding.forum.api.model.dto.ArticleKafkaMessageDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * kafka监听文章数据
 *
 * @ClassName: ArticleKafkaListener
 * @Author: ygl
 * @Date: 2023/6/15 14:26
 * @Version: 1.0
 */
@Component
@Slf4j
public class ArticleKafkaListener {

    @KafkaListener(topics = {KafkaTopicConstant.ARTICLE_TOPIC})
    public void consumer(ConsumerRecord<?, ?> consumerRecord) {

        log.info("监听中、、、");
        Optional<?> value = Optional.ofNullable(consumerRecord.value());

        if (value.isPresent()) {
            String msg = value.get().toString();
            String msgStr = JSONObject.toJSONString(msg);
            ArticleKafkaMessageDTO articleKafkaMessageDTO = JSONObject.parseObject(msg, ArticleKafkaMessageDTO.class);
            log.info("消费消息：{}", msgStr);
        }
    }

}
