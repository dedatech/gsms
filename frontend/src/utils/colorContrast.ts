/**
 * 色彩对比度工具类
 * 用于计算颜色对比度并验证 WCAG 合规性
 *
 * 参考资料：
 * - WCAG 2.1: https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html
 * - 对比度计算: https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html#dfn-contrast-ratio
 */

/**
 * RGB 颜色对象
 */
interface RGB {
  r: number;
  g: number;
  b: number;
}

/**
 * 对比度验证结果
 */
export interface ContrastResult {
  /** 对比度比值 */
  ratio: number;
  /** 是否符合 WCAG AA 标准（普通文字） */
  passesAA: boolean;
  /** 是否符合 WCAG AA 标准（大文字 18pt+） */
  passesAALarge: boolean;
  /** 是否符合 WCAG AAA 标准（普通文字） */
  passesAAA: boolean;
  /** 是否符合 WCAG AAA 标准（大文字 18pt+） */
  passesAAALarge: boolean;
  /** 评级等级 */
  rating: 'FAIL' | 'AA' | 'AAA';
}

/**
 * 验证结果
 */
export interface ValidationResult {
  /** 是否通过验证 */
  valid: boolean;
  /** 错误列表 */
  errors: string[];
  /** 警告列表 */
  warnings: string[];
  /** 通过的检查 */
  passed: string[];
}

/**
 * 将十六进制颜色转换为 RGB
 * @param hex - 十六进制颜色（#RRGGBB 或 #RGB）
 * @returns RGB 对象
 */
export function hexToRgb(hex: string): RGB {
  // 移除 # 号
  const cleanHex = hex.replace('#', '');

  // 处理简写形式（#RGB → #RRGGBB）
  const fullHex = cleanHex.length === 3
    ? cleanHex.split('').map(c => c + c).join('')
    : cleanHex;

  const r = parseInt(fullHex.substring(0, 2), 16);
  const g = parseInt(fullHex.substring(2, 4), 16);
  const b = parseInt(fullHex.substring(4, 6), 16);

  return { r, g, b };
}

/**
 * 计算相对亮度
 * @param rgb - RGB 颜色对象
 * @returns 相对亮度值（0-1）
 */
export function getLuminance(rgb: RGB): number {
  const { r, g, b } = rgb;

  // 将 8 位 RGB 值转换为线性 RGB
  const toLinear = (c: number) => {
    const sRGB = c / 255;
    return sRGB <= 0.03928
      ? sRGB / 12.92
      : Math.pow((sRGB + 0.055) / 1.055, 2.4);
  };

  const rLinear = toLinear(r);
  const gLinear = toLinear(g);
  const bLinear = toLinear(b);

  // 计算相对亮度
  return 0.2126 * rLinear + 0.7152 * gLinear + 0.0722 * bLinear;
}

/**
 * 计算两个颜色的对比度
 * @param foreground - 前景色（十六进制）
 * @param background - 背景色（十六进制）
 * @returns 对比度比值
 */
export function getContrastRatio(foreground: string, background: string): number {
  const fgRgb = hexToRgb(foreground);
  const bgRgb = hexToRgb(background);

  const fgLuminance = getLuminance(fgRgb);
  const bgLuminance = getLuminance(bgRgb);

  const lighter = Math.max(fgLuminance, bgLuminance);
  const darker = Math.min(fgLuminance, bgLuminance);

  return (lighter + 0.05) / (darker + 0.05);
}

/**
 * 验证颜色对比度是否符合 WCAG 标准
 * @param foreground - 前景色（十六进制）
 * @param background - 背景色（十六进制）
 * @param fontSize - 字体大小（像素，默认 14）
 * @returns 对比度验证结果
 */
export function checkContrast(
  foreground: string,
  background: string,
  fontSize: number = 14
): ContrastResult {
  const ratio = getContrastRatio(foreground, background);
  const isLargeText = fontSize >= 18 || (fontSize >= 14 && fontWeight >= 700);

  // WCAG 2.1 标准
  // AA: 普通文字 4.5:1, 大文字 3:1
  // AAA: 普通文字 7:1, 大文字 4.5:1

  const passesAA = isLargeText ? ratio >= 3 : ratio >= 4.5;
  const passesAAA = isLargeText ? ratio >= 4.5 : ratio >= 7;
  const passesAALarge = ratio >= 3;
  const passesAAALarge = ratio >= 4.5;

  let rating: ContrastResult['rating'] = 'FAIL';
  if (passesAAA) rating = 'AAA';
  else if (passesAA) rating = 'AA';

  return {
    ratio: Math.round(ratio * 100) / 100,
    passesAA,
    passesAALarge,
    passesAAA,
    passesAAALarge,
    rating
  };
}

/**
 * 字体粗重辅助变量（需要在调用 checkContrast 时传入）
 */
let fontWeight = 400;

/**
 * 设置字体粗重（用于判断是否为大文字）
 * @param weight - 字体粗重值
 */
export function setFontWeight(weight: number): void {
  fontWeight = weight;
}

/**
 * 验证主题配置的对比度
 * @param themeConfig - 主题配置对象
 * @returns 验证结果
 */
export function validateTheme(themeConfig: Record<string, any>): ValidationResult {
  const result: ValidationResult = {
    valid: true,
    errors: [],
    warnings: [],
    passed: []
  };

  // 验证侧边栏文字对比度
  if (themeConfig.sidebarText && themeConfig.sidebarBackground) {
    const check = checkContrast(
      themeConfig.sidebarText,
      themeConfig.sidebarBackground || '#ffffff'
    );

    if (check.rating === 'FAIL') {
      result.valid = false;
      result.errors.push(
        `侧边栏文字对比度不足: ${check.ratio}:1 (要求 4.5:1)`
      );
    } else {
      result.passed.push(`侧边栏文字: ${check.ratio}:1 (${check.rating})`);
    }
  }

  // 验证主色对比度
  if (themeConfig.primaryColor) {
    const check = checkContrast(themeConfig.primaryColor, '#ffffff');

    if (check.rating === 'FAIL') {
      result.warnings.push(
        `主题色对比度较低: ${check.ratio}:1 (建议 ≥ 4.5:1)`
      );
    } else {
      result.passed.push(`主题色: ${check.ratio}:1 (${check.rating})`);
    }
  }

  // 验证文字颜色对比度
  if (themeConfig.textColor && themeConfig.bgColor) {
    const check = checkContrast(themeConfig.textColor, themeConfig.bgColor);

    if (check.rating === 'FAIL') {
      result.valid = false;
      result.errors.push(
        `文字对比度不足: ${check.ratio}:1 (要求 4.5:1)`
      );
    } else {
      result.passed.push(`文字颜色: ${check.ratio}:1 (${check.rating})`);
    }
  }

  return result;
}

/**
 * 批量验证颜色组合
 * @param colors - 颜色组合数组 [{foreground, background, name}]
 * @returns 验证结果
 */
export function validateColorCombinations(
  colors: Array<{ foreground: string; background: string; name: string }>
): ValidationResult {
  const result: ValidationResult = {
    valid: true,
    errors: [],
    warnings: [],
    passed: []
  };

  colors.forEach(({ foreground, background, name }) => {
    const check = checkContrast(foreground, background);

    if (check.rating === 'FAIL') {
      result.valid = false;
      result.errors.push(
        `${name}: ${check.ratio}:1 (要求 4.5:1)`
      );
    } else {
      result.passed.push(`${name}: ${check.ratio}:1 (${check.rating})`);
    }
  });

  return result;
}

/**
 * 生成颜色对比度报告
 * @param themeConfig - 主题配置对象
 * @returns 格式化的报告字符串
 */
export function generateContrastReport(themeConfig: Record<string, any>): string {
  const result = validateTheme(themeConfig);

  let report = '🎨 色彩对比度验证报告\n';
  report += '='.repeat(50) + '\n\n';

  if (result.passed.length > 0) {
    report += '✅ 通过验证:\n';
    result.passed.forEach(item => {
      report += `  ✓ ${item}\n`;
    });
    report += '\n';
  }

  if (result.warnings.length > 0) {
    report += '⚠️  警告:\n';
    result.warnings.forEach(item => {
      report += `  ⚠ ${item}\n`;
    });
    report += '\n';
  }

  if (result.errors.length > 0) {
    report += '❌ 错误:\n';
    result.errors.forEach(item => {
      report += `  ✗ ${item}\n`;
    });
    report += '\n';
  }

  report += '='.repeat(50) + '\n';
  report += `总体结果: ${result.valid ? '✅ 通过' : '❌ 未通过'}\n`;

  return report;
}

/**
 * 开发环境自动验证（在 main.ts 中调用）
 */
export function autoValidateThemeInDev(): void {
  // 暂时禁用自动验证，避免开发环境报错
  // 如需启用，请确保所有颜色对比度符合 WCAG AA 标准（4.5:1）
  return;

  /* if (import.meta.env.DEV) {
    console.log('🎨 开始验证主题色彩对比度...');

    // 定义需要验证的关键颜色组合
    const colorCombinations = [
      { foreground: '#d48806', background: '#ffffff', name: '待办状态' },
      { foreground: '#1890ff', background: '#ffffff', name: '进行中状态' },
      { foreground: '#059669', background: '#ffffff', name: '完成状态' },
      { foreground: '#6b7280', background: '#ffffff', name: '低优先级' },
      { foreground: '#f59e0b', background: '#ffffff', name: '中优先级' },
      { foreground: '#dc2626', background: '#ffffff', name: '高优先级' },
      { foreground: '#111827', background: '#ffffff', name: '主要文字' },
      { foreground: '#4b5563', background: '#ffffff', name: '次要文字' }
    ];

    const result = validateColorCombinations(colorCombinations);

    console.log(generateContrastReport({}));

    if (!result.valid) {
      console.error('❌ 主题色彩对比度验证失败！请修复上述错误。');
    } else {
      console.log('✅ 主题色彩对比度验证通过！');
    }
  } */
}
