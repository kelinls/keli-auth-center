package com.keli.tokenserver.common.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 通用参数校验工具类
 * 提供常用的参数校验方法，减少重复代码
 */
public class ValidationUtils {

    /**
     * 校验字符串是否为null或空
     * @param str 待校验的字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 校验字符串是否为null或空白
     * @param str 待校验的字符串
     * @return 是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 校验集合是否为null或空
     * @param collection 待校验的集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 校验Map是否为null或空
     * @param map 待校验的Map
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 校验对象是否为null
     * @param obj 待校验的对象
     * @return 是否为null
     */
    public static boolean isNull(Object obj) {
        return Objects.isNull(obj);
    }

    /**
     * 校验对象是否不为null
     * @param obj 待校验的对象
     * @return 是否不为null
     */
    public static boolean nonNull(Object obj) {
        return Objects.nonNull(obj);
    }

    /**
     * 校验参数，如果为true则抛出异常
     * @param expression 表达式结果
     * @param message 异常消息
     * @throws IllegalArgumentException 当表达式为true时抛出
     */
    public static void validateArgument(boolean expression, String message) {
        if (expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 校验参数是否为空，如果为空则抛出异常
     * @param obj 待校验的对象
     * @param message 异常消息
     * @throws IllegalArgumentException 当对象为空时抛出
     */
    public static void notNull(Object obj, String message) {
        validateArgument(isNull(obj), message);
    }

    /**
     * 校验字符串是否为空，如果为空则抛出异常
     * @param str 待校验的字符串
     * @param message 异常消息
     * @throws IllegalArgumentException 当字符串为空时抛出
     */
    public static void notEmpty(String str, String message) {
        validateArgument(isEmpty(str), message);
    }

    /**
     * 校验字符串是否为空白，如果为空白则抛出异常
     * @param str 待校验的字符串
     * @param message 异常消息
     * @throws IllegalArgumentException 当字符串为空白时抛出
     */
    public static void notBlank(String str, String message) {
        validateArgument(isBlank(str), message);
    }

    /**
     * 校验集合是否为空，如果为空则抛出异常
     * @param collection 待校验的集合
     * @param message 异常消息
     * @throws IllegalArgumentException 当集合为空时抛出
     */
    public static void notEmpty(Collection<?> collection, String message) {
        validateArgument(isEmpty(collection), message);
    }

    /**
     * 校验Map是否为空，如果为空则抛出异常
     * @param map 待校验的Map
     * @param message 异常消息
     * @throws IllegalArgumentException 当Map为空时抛出
     */
    public static void notEmpty(Map<?, ?> map, String message) {
        validateArgument(isEmpty(map), message);
    }

    /**
     * 校验两个对象是否相等，如果不相等则抛出异常
     * @param obj1 第一个对象
     * @param obj2 第二个对象
     * @param message 异常消息
     * @throws IllegalArgumentException 当两个对象不相等时抛出
     */
    public static void equals(Object obj1, Object obj2, String message) {
        validateArgument(!Objects.equals(obj1, obj2), message);
    }

    /**
     * 校验数值是否在指定范围内
     * @param value 待校验的数值
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @param message 异常消息
     * @throws IllegalArgumentException 当数值不在范围内时抛出
     */
    public static void between(int value, int min, int max, String message) {
        validateArgument(value < min || value > max, message);
    }

    /**
     * 校验数值是否大于最小值
     * @param value 待校验的数值
     * @param min 最小值（不包含）
     * @param message 异常消息
     * @throws IllegalArgumentException 当数值不大于最小值时抛出
     */
    public static void greaterThan(int value, int min, String message) {
        validateArgument(value <= min, message);
    }

    /**
     * 校验数值是否小于最大值
     * @param value 待校验的数值
     * @param max 最大值（不包含）
     * @param message 异常消息
     * @throws IllegalArgumentException 当数值不小于最大值时抛出
     */
    public static void lessThan(int value, int max, String message) {
        validateArgument(value >= max, message);
    }

    /**
     * 校验字符串是否匹配正则表达式
     * @param str 待校验的字符串
     * @param regex 正则表达式
     * @param message 异常消息
     * @throws IllegalArgumentException 当字符串不匹配正则表达式时抛出
     */
    public static void matches(String str, String regex, String message) {
        validateArgument(str == null || !Pattern.matches(regex, str), message);
    }
}