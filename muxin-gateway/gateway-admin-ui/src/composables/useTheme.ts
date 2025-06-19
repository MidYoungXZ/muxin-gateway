// 主题类型
export type ThemeType = 'light' | 'dark';

// 主题状态
const theme = ref<ThemeType>('light');

/**
 * Applies the given theme to the document root by setting the class name
 * and updating a data-theme attribute.
 * @param themeType The theme type to apply.
 */
export function applyTheme(themeType: ThemeType) {
  const root = document.documentElement;
  
  // 移除所有主题类名
  root.classList.remove('light', 'dark');
  
  // 添加新的主题类名
  root.classList.add(themeType);
  
  // 为了兼容，同时设置 body 的类名
  document.body.classList.remove('light', 'dark');
  document.body.classList.add(themeType);
  
  // 保存到 localStorage
  localStorage.setItem('theme', themeType);
}

/**
 * Initializes the theme from localStorage or system preference.
 * Must be called once at application startup, before the Vue app is mounted.
 */
export function initTheme() {
  // 从 localStorage 读取主题设置，默认为 light
  const savedTheme = localStorage.getItem('theme') as ThemeType || 'light';
  theme.value = savedTheme;
  applyTheme(savedTheme);
}

/**
 * A composable to manage and react to theme changes.
 *
 * @returns An object with the current theme type, and functions to set or toggle the theme.
 */
export function useTheme() {
  // 监听主题变化
  watch(theme, (newTheme: ThemeType) => {
    applyTheme(newTheme);
  });
  
  /**
   * Sets the theme to the specified type.
   * @param themeType The theme type to set.
   */
  const setTheme = (themeType: ThemeType) => {
    theme.value = themeType;
    applyTheme(themeType);
  };

  /**
   * Toggles the theme between 'light' and 'dark'.
   */
  const toggleTheme = () => {
    const newTheme = theme.value === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
  };

  return {
    theme,
    setTheme,
    toggleTheme,
    initTheme
  };
} 