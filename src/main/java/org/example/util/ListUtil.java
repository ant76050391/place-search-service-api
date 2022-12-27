package org.example.util;

import java.util.*;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;

public class ListUtil {
  private static <T> ArrayList<T> unionBy(List<T> list1, List<T> list2) {
    // NOTE : 합집합
    Set<T> set = new HashSet<T>();
    set.addAll(list1);
    set.addAll(list2);
    return new ArrayList<T>(set);
  }

  public static <T> ArrayList<T> intersectionBy(List<T> list1, List<T> list2) {
    // NOTE :  교집합
    ArrayList<T> result = new ArrayList<>();
    result.addAll(list1);
    result.retainAll(list2);
    return result;
  }

  public static <T> ArrayList<T> differenceBy(List<T> list1, List<T> list2) {
    // NOTE :  차집합
    ArrayList<T> result = new ArrayList<>();
    result.addAll(list1);
    result.removeAll(list2);
    return result;
  }

  public static <S, T> List<T> mappingLists(
      ModelMapper modelMapper, List<S> source, Class<T> targetClass) {
    return source.stream()
        .map(element -> modelMapper.map(element, targetClass))
        .collect(Collectors.toList());
  }
}
