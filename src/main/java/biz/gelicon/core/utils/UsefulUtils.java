package biz.gelicon.core.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class UsefulUtils {


    public static <T> Collector<T, ?, List<T>> toListReversed() {
        return Collectors.collectingAndThen(Collectors.toList(), l -> {
            Collections.reverse(l);
            return l;
        });
    }

    public static int indexOf(int[] data, int sample) {
        for (int i = 0; i < data.length; i++) {
            if(data[i]==sample) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Гененирует строку определенной длины
     * @param character
     * @param length
     * @return
     */
    public static String createString(char character, int length) {
        char[] chars = new char[length];
        Arrays.fill(chars, character);
        return new String(chars);
    }

}
