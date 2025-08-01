package com.github.paicoding.forum.service.article.helper;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleGroupDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author YiHui
 * @date 2025/7/30
 */
public class TreeBuilder {

    public static <T> List<T> buildTree(List<T> list
            , Function<T, Long> idKey
            , Function<T, Long> pidKey
            , Function<T, Integer> sortFunc
            , Function<T, List<T>> childGetFunc
            , BiConsumer<T, List<T>> updateChildFunc
    ) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 创建Map存储所有节点，key为groupId，value为节点对象
        Map<Long, T> nodeMap = new HashMap<>();
        for (T node : list) {
            nodeMap.put(idKey.apply(node), node);
        }

        // 存储根节点列表
        List<T> rootNodes = new ArrayList<>();

        // 构建树结构
        for (T node : list) {
            Long parentGroupId = pidKey.apply(node);

            // 如果parentGroupId为null或不存在于nodeMap中，则为根节点
            if (parentGroupId == null || parentGroupId == 0 || !nodeMap.containsKey(parentGroupId)) {
                rootNodes.add(node);
            } else {
                // 找到父节点，并将当前节点添加到父节点的children中
                T parentNode = nodeMap.get(parentGroupId);
                if (childGetFunc.apply(parentNode) == null) {
                    updateChildFunc.accept(parentNode, new ArrayList<>());
                }

                childGetFunc.apply(parentNode).add(node);
            }
        }

        // 对所有节点的子节点按section升序排序
        sortChildrenBySection(nodeMap.values(), sortFunc, childGetFunc);

        // 对根节点按section升序排序
        rootNodes.sort(Comparator.comparing(sortFunc::apply));

        return rootNodes;
    }

    /**
     * 递归对所有节点的子节点按section排序
     *
     * @param nodes 节点集合
     */
    private static <T> void sortChildrenBySection(Collection<T> nodes
            , Function<T, Integer> sortFunc
            , Function<T, List<T>> childGetFunc) {
        for (T node : nodes) {
            List<T> child = childGetFunc.apply(node);
            if (CollectionUtils.isNotEmpty(child)) {
                // 按section升序排序
                child.sort(Comparator.comparing(sortFunc::apply));
                // 递归处理子节点
                sortChildrenBySection(child, sortFunc, childGetFunc);
            }
        }
    }

    /**
     * 将列表转换为树结构
     *
     * @param list 原始列表数据
     * @return 树结构的根节点列表
     */
    public static List<ColumnArticleGroupDTO> buildTree(List<ColumnArticleGroupDTO> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 创建Map存储所有节点，key为groupId，value为节点对象
        Map<Long, ColumnArticleGroupDTO> nodeMap = new HashMap<>();
        for (ColumnArticleGroupDTO node : list) {
            nodeMap.put(node.getGroupId(), node);
        }

        // 存储根节点列表
        List<ColumnArticleGroupDTO> rootNodes = new ArrayList<>();

        // 构建树结构
        for (ColumnArticleGroupDTO node : list) {
            Long parentGroupId = node.getParentGroupId();

            // 如果parentGroupId为null或不存在于nodeMap中，则为根节点
            if (parentGroupId == null || parentGroupId == 0 || !nodeMap.containsKey(parentGroupId)) {
                rootNodes.add(node);
            } else {
                // 找到父节点，并将当前节点添加到父节点的children中
                ColumnArticleGroupDTO parentNode = nodeMap.get(parentGroupId);
                if (parentNode.getChildren() == null) {
                    parentNode.setChildren(new ArrayList<>());
                }
                parentNode.getChildren().add(node);
            }
        }

        // 对所有节点的子节点按section升序排序
        sortChildrenBySection(nodeMap.values());

        // 对根节点按section升序排序
        rootNodes.sort(Comparator.comparing(ColumnArticleGroupDTO::getSection));

        return rootNodes;
    }

    /**
     * 递归对所有节点的子节点按section排序
     *
     * @param nodes 节点集合
     */
    private static void sortChildrenBySection(Collection<ColumnArticleGroupDTO> nodes) {
        for (ColumnArticleGroupDTO node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                // 按section升序排序
                node.getChildren().sort(Comparator.comparing(ColumnArticleGroupDTO::getSection));
                // 递归处理子节点
                sortChildrenBySection(node.getChildren());
            }
        }
    }
}
