package io.mybatis.provider.defaults;

import io.mybatis.provider.*;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通过 {@link SqlWrapper} 注解支持对 SQL 的扩展
 *
 * @author liuzh
 */
public class AnnotationSqlScriptWrapper implements SqlScriptWrapper {

  @Override
  public SqlScript wrap(ProviderContext context, EntityTable entity, SqlScript sqlScript) {
    Class<?> mapperType = context.getMapperType();
    Method mapperMethod = context.getMapperMethod();
    //接口注解
    List<AbstractSqlScriptWrapper> wrappers = parseAnnotations(mapperType, ElementType.TYPE, mapperType.getAnnotations());
    //方法注解
    wrappers.addAll(parseAnnotations(mapperMethod, ElementType.METHOD, mapperMethod.getAnnotations()));
    //参数注解
    Parameter[] parameters = mapperMethod.getParameters();
    Annotation[][] parameterAnnotations = mapperMethod.getParameterAnnotations();
    for (int i = 0; i < parameters.length; i++) {
      wrappers.addAll(parseAnnotations(parameters[i], ElementType.PARAMETER, parameterAnnotations[i]));
    }
    //去重，排序
    wrappers = wrappers.stream().distinct().sorted(Comparator.comparing(f -> ((Order) f).getOrder()).reversed()).collect(Collectors.toList());
    for (SqlScriptWrapper wrapper : wrappers) {
      sqlScript = wrapper.wrap(context, entity, sqlScript);
    }
    return sqlScript;
  }

  /**
   * 获取对象上的 AbstractSqlScriptWrapper 实例
   *
   * @param target
   * @param type
   * @param annotations
   * @return
   */
  private List<AbstractSqlScriptWrapper> parseAnnotations(Object target, ElementType type, Annotation[] annotations) {
    List<Class<? extends AbstractSqlScriptWrapper>> classes = new ArrayList<>();
    for (int i = 0; i < annotations.length; i++) {
      Annotation annotation = annotations[i];
      Class<? extends Annotation> annotationType = annotation.annotationType();
      if (annotationType == SqlWrapper.class) {
        classes.addAll(Arrays.asList(((SqlWrapper) annotation).value()));
      } else if (annotationType.isAnnotationPresent(SqlWrapper.class)) {
        SqlWrapper annotationTypeAnnotation = annotationType.getAnnotation(SqlWrapper.class);
        classes.addAll(Arrays.asList(annotationTypeAnnotation.value()));
      }
    }
    return classes.stream().map(c -> (AbstractSqlScriptWrapper) newInstance(c, target, type, annotations))
      .collect(Collectors.toList());
  }

  /**
   * 实例化 AbstractSqlScriptWrapper 对象
   *
   * @param instanceClass
   * @param target
   * @param type
   * @param annotations
   * @param <T>
   * @return
   */
  public <T> T newInstance(Class instanceClass, Object target, ElementType type, Annotation[] annotations) {
    try {
      return (T) instanceClass.getConstructor(Object.class, ElementType.class, Annotation[].class).newInstance(target, type, annotations);
    } catch (Exception e) {
      throw new RuntimeException("实例化[ " + instanceClass + " ]失败", e);
    }
  }

}
