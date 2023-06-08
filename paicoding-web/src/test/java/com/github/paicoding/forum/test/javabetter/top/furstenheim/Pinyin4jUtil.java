package com.github.paicoding.forum.test.javabetter.top.furstenheim;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/27/22
 */

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.HashSet;
import java.util.Set;

public class Pinyin4jUtil {

    /**
     * getFirstSpellPinYin:(多音字的时候获取第一个). <br/>
     * @param src  传入的拼音字符串，以逗号隔开
     * @param isFullSpell 是否全拼，true:全拼，false:第一个汉字全拼(其它汉字取首字母)
     * @return 第一个拼音
     */
    public static String getFirstSpellPinYin(String src , boolean isFullSpell) {
        String targetStr = Pinyin4jUtil.makeStringByStringSet(Pinyin4jUtil.getPinyin(src, isFullSpell));
        String[] split = targetStr.split(",");
        if (split.length > 1) {
            targetStr = split[0];
        }
        return targetStr;
    }

    /**
     * makeStringByStringSet:(拼音字符串集合转换字符串(逗号分隔)). <br/>
     * @param stringSet  拼音集合
     * @return  带逗号字符串
     */
    public static String makeStringByStringSet(Set<String> stringSet) {
        StringBuilder str = new StringBuilder();
        int i = 0;
        if (stringSet.size() > 0) {
            for (String s : stringSet) {
                if (i == stringSet.size() - 1) {
                    str.append(s);
                } else {
                    str.append(s + ",");
                }
                i++;
            }
        }
        return str.toString().toLowerCase();
    }

    /**
     * getPinyin:(获取汉字拼音). <br/>
     * @param src   汉字
     * @return
     */
    public static Set<String> getPinyin(String src, boolean isFullSpell) {
        if (src != null && !src.trim().equalsIgnoreCase("")) {
            char[] srcChar;
            srcChar = src.toCharArray();
            // 汉语拼音格式输出类
            HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();

            // 输出设置，大小写，音标方式等
            hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

            String[][] temp = new String[src.length()][];
            for (int i = 0; i < srcChar.length; i++) {
                char c = srcChar[i];
                if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")) {//中文
                    try {
                        temp[i] = PinyinHelper.toHanyuPinyinStringArray(
                                srcChar[i], hanYuPinOutputFormat);
                        if (!isFullSpell) {
                            if (i == 0) {
                                temp[i] = temp[i];
                            } else {
                                String[] tTemps = new String[temp[i].length];
                                for (int j = 0; j < temp[i].length; j++) {
                                    char t = temp[i][j].charAt(0);
                                    tTemps[j] = Character.toString(t);
                                }
                                temp[i] = tTemps;
                            }
                        }
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                } else if (((int) c >= 65 && (int) c <= 90)
                        || ((int) c >= 97 && (int) c <= 122)) {//英文
                    temp[i] = new String[] { String.valueOf(srcChar[i]) };
                } else {
                    temp[i] = new String[] { "" };
                }
            }
            String[] pingyinArray = exchange(temp);
            Set<String> pinyinSet = new HashSet<String>();
            for (int i = 0; i < pingyinArray.length; i++) {
                pinyinSet.add(pingyinArray[i]);
            }
            return pinyinSet;
        }
        return null;
    }

    /**
     * 递归
     * @param strJaggedArray
     * @return
     */
    public static String[] exchange(String[][] strJaggedArray) {
        String[][] temp = doExchange(strJaggedArray);
        return temp[0];
    }

    /**
     * 递归
     * @param strJaggedArray
     * @return
     */
    private static String[][] doExchange(String[][] strJaggedArray) {
        int len = strJaggedArray.length;
        if (len >= 2) {
            int len1 = strJaggedArray[0].length;
            int len2 = strJaggedArray[1].length;
            int newlen = len1 * len2;
            String[] temp = new String[newlen];
            int Index = 0;
            for (int i = 0; i < len1; i++) {
                for (int j = 0; j < len2; j++) {
                    temp[Index] = strJaggedArray[0][i] + strJaggedArray[1][j];
                    Index++;
                }
            }
            String[][] newArray = new String[len - 1][];
            for (int i = 2; i < len; i++) {
                newArray[i - 1] = strJaggedArray[i];
            }
            newArray[0] = temp;
            return doExchange(newArray);
        } else {
            return strJaggedArray;
        }
    }


}
