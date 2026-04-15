package com.example.demofirst.utils;

import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 自定义集合工具类（解决空指针、性能、通用转换问题）
 * 替代原生Collections，适配项目实际场景
 */
public class CollectionUtils {

    /**
     * 判空（包含null和空集合）
     */
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 非空判断
     */
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    /**
     * ArrayList初始化指定容量（避免扩容）
     */
    public static <T> List<T> newArrayListWithCapacity(int initialCapacity) {
        Assert.isTrue(initialCapacity >= 0, "初始容量不能为负数");
        return new ArrayList<>(initialCapacity);
    }

    /**
     * 列表转Map（避免重复key）
     * @param list 源列表
     * @param keyExtractor key提取器
     * @return 安全的Map（重复key取第一个）
     */
    public static <T, K> Map<K, T> listToMap(List<T> list, Function<T, K> keyExtractor) {
        if (isEmpty(list)) {
            return new HashMap<>(0);
        }
        // 初始容量设置为list.size()，避免扩容
        Map<K, T> resultMap = new HashMap<>(list.size());
        for (T t : list) {
            K key = keyExtractor.apply(t);
            // 重复key只保留第一个
            if (!resultMap.containsKey(key)) {
                resultMap.put(key, t);
            }
        }
        return resultMap;
    }

    /**
     * 高并发场景下的Map（ConcurrentHashMap）
     */
    public static <K, V> Map<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<>();
    }


}
