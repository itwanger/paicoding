package com.github.liuyueyi.forum.service.comment.repository;

import com.github.liuyueyi.forum.service.comment.repository.CommentRepository;
import com.github.liuyueyi.forum.service.comment.repository.mapper.CommentMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 评论相关DB操作
 *
 * @author louzai
 * @date 2022-07-18
 */
@Service
public class CommentRepositoryImpl implements CommentRepository {

    @Resource
    private CommentMapper commentMapper;

}