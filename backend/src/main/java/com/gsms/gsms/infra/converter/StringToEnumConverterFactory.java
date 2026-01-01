package com.gsms.gsms.infra.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * String 到枚举的通用转换器工厂
 * 支持通过 code（Integer）转换为枚举
 */
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    private static final Logger log = LoggerFactory.getLogger(StringToEnumConverterFactory.class);

    @Override
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        log.info("Creating converter for enum type: {}", targetType.getName());
        return new StringToEnumConverter<>(targetType);
    }

    /**
     * 具体的转换器实现
     */
    private static class StringToEnumConverter<T extends Enum<?>> implements Converter<String, T> {
        private static final Logger log = LoggerFactory.getLogger(StringToEnumConverter.class);
        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T convert(String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }

            log.debug("Converting string '{}' to enum type: {}", source, enumType.getSimpleName());

            try {
                // 尝试调用枚举类的 fromCode 方法（如果存在）
                return (T) enumType.getMethod("fromCode", Integer.class).invoke(null, Integer.valueOf(source));
            } catch (Exception e) {
                log.debug("fromCode method failed, trying valueOf: {}", e.getMessage());
                // 如果 fromCode 方法不存在或调用失败，尝试通过 name 转换
                try {
                    return (T) enumType.getMethod("valueOf", String.class).invoke(null, source.toUpperCase());
                } catch (Exception ex) {
                    log.error("Failed to convert '{}' to enum {}", source, enumType.getSimpleName());
                    throw new IllegalArgumentException(
                        String.format("无法将字符串 '%s' 转换为枚举类型 %s", source, enumType.getSimpleName())
                    );
                }
            }
        }
    }
}
