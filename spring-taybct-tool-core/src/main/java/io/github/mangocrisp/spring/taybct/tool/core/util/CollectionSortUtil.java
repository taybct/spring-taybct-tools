package io.github.mangocrisp.spring.taybct.tool.core.util;

import cn.hutool.core.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;

/**
 * 集合排序工具
 *
 * @author xijieyin <br> 2022/8/5 19:18
 * @since 1.0.0
 */
@Slf4j
public class CollectionSortUtil {

    private final static String REGEX = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";

    public static <T> LinkedHashSet<T> sortListByName(LinkedHashSet<T> source) {
        return sortListByName(source, "name", true);
    }

    public static <T> LinkedHashSet<T> sortListByName(LinkedHashSet<T> source, String name) {
        return sortListByName(source, name, true);
    }

    public static <T> LinkedHashSet<T> sortListByName(LinkedHashSet<T> source, String name, Boolean asc) {
        if (CollectionUtils.isEmpty(source)) {
            return source;
        }
        return new LinkedHashSet<>(sortListByName(new ArrayList<>(source), name, asc));
    }

    public static <T> List<T> sortListByName(List<T> source) {
        return sortListByName(source, "name");
    }

    public static <T> List<T> sortListByName(List<T> source, String name) {
        return sortListByName(source, name, true);
    }

    /**
     * 排序
     *
     * @param source   源集合
     * @param sortName 按 sortName 排序
     * @param asc      true 正序 false 倒序
     * @return {@code List<T>}
     * @author xijieyin <br> 2022/8/5 19:18
     * @since 1.0.0
     */
    public static <T> List<T> sortListByName(List<T> source, String sortName, Boolean asc) {
        Class<T> clazz;
        if (CollectionUtils.isEmpty(source)) {
            return new ArrayList<>();
        }
        clazz = (Class<T>) source.get(0).getClass();
        Field field = ClassUtil.getDeclaredField(clazz, sortName);
        if (null == field) {
            return source;
        }
        field.setAccessible(true);
        try {
            source.sort(comparingAny(t -> {
                try {
                    return field.get(t);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }, asc));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return source;
    }

    /**
     * 用任何类型比较
     *
     * @param getValue 获取值
     * @param <T>      类型
     * @return 比较结果
     */
    public static <T> Comparator<T> comparingAny(Function<T, ?> getValue) {
        return comparingAny(getValue, true);
    }

    /**
     * 用任何类型比较（包括汉语拼音），规则如下：
     * <br>
     * 逐个比较字符，先比较数字，如果前面的数字都相等就比较后面的英文，英文用汉语拼音转换
     *
     * @param getValue 获取值
     * @param asc      正序？
     * @param <T>      类型
     * @return 比较结果
     */
    public static <T> Comparator<T> comparingAny(Function<T, ?> getValue, boolean asc) {
        return (o1, o2) -> {
            // 如果有排序字段是空的，那空的字段就要往后面排，也就是说，如果是 o2 = null 那 o2 就得排在后面
            Collator cmp = Collator.getInstance(Locale.CHINA);
            Optional<Object> optional1;
            Optional<Object> optional2;
            optional1 = Optional.ofNullable(getValue.apply(o1));
            optional2 = Optional.ofNullable(getValue.apply(o2));
            if (optional1.isEmpty() || optional2.isEmpty()) {
                if (optional2.isEmpty() && optional1.isPresent()) {
                    return (asc ? 1 : -1);
                } else if (optional2.isPresent()) {
                    return -1 * (asc ? 1 : -1);
                }
                return 0;
            }
            String name1 = optional1.get().toString().replaceAll(REGEX, "");
            String name2 = optional2.get().toString().replaceAll(REGEX, "");


            for (int i = 0; i < name1.length() && i < name2.length(); i++) {
                if (name1.charAt(i) != name2.charAt(i) || (Character.isDigit(name1.charAt(i)) && Character.isDigit(name2.charAt(i)))) {
                    //如果两个字符不再相等而且都是数字，就直接比较数字
                    if (Character.isDigit(name1.charAt(i)) && Character.isDigit(name2.charAt(i))) {
                        //如果是数字，就把后面的所有的不是数字的字符替换成 _ ，然后按照 _ 分成数组，只比较数组的第一个，如果第一个相等，就再比较数组的第二个，一直比较下去，直到没得比
                        name1 = name1.substring(i);
                        name2 = name2.substring(i);
                        //装数据的数组
                        String[] numArr1 = removeEmptyChar(name1.replaceAll("\\D", "_").split("_"));
                        String[] numArr2 = removeEmptyChar(name2.replaceAll("\\D", "_").split("_"));

                        String[] charArr1 = removeEmptyChar(name1.replaceAll("\\d", "_").split("_"));
                        String[] charArr2 = removeEmptyChar(name1.replaceAll("\\d", "_").split("_"));
                        for (int j = 0; j < numArr1.length && j < numArr2.length; j++) {
                            if (!numArr1[j].equalsIgnoreCase(numArr2[j])) {
                                //如果先比较数字是不相等的，就可以直接比较哪个大，哪个小了
                                return (Integer.parseInt(numArr1[j]) - Integer.parseInt(numArr2[j])) * (asc ? 1 : -1);
                            } else {
                                //如果先比较的数字是相等的，就要再比较数字后面的字符是否相等，如果相等，就进行下一个循环来再比较数字
                                //如果字符不相等，就直接跳出了，进入到下面的逻辑 按中文比较
                                if (j < charArr1.length && j < charArr2.length) {
                                    if (!charArr1[j].equalsIgnoreCase(charArr2[j])) {
                                        break;
                                    }
                                }
                            }
                        }
                        return 0;
                    } else {
                        //如果有一个不是数字，那就没有可比性
                        break;
                    }
                }
            }
            //汉语拼音解决多音字问题 如 重(chong)庆
            HanyuPinyinOutputFormat pinyinOutputFormat = new HanyuPinyinOutputFormat();
            try {
                name1 = PinyinHelper.toHanYuPinyinString(name1, pinyinOutputFormat, " ", true);
                name2 = PinyinHelper.toHanYuPinyinString(name2, pinyinOutputFormat, " ", true);
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                throw new RuntimeException(badHanyuPinyinOutputFormatCombination);
            }
            int compared = cmp.compare(name1, name2);
            if (compared > 0) {
                return (asc ? 1 : -1);
            } else if (compared < 0) {
                return -1 * (asc ? 1 : -1);
            }
            return 0;

        };
    }

    /**
     * 移除字段里面的空字符
     *
     * @param source 源字段
     * @return String[]
     * @author xijieyin <br> 2022/8/5 19:34
     * @since 1.0.0
     */
    public static String[] removeEmptyChar(String[] source) {
        List<String> target = new ArrayList<>();
        for (String s : source) {
            if (StringUtils.isNotBlank(s)) {
                target.add(s);
            }
        }
        return target.toArray(new String[target.size()]);
    }
}
