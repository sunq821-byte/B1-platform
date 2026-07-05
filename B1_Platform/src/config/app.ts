/** 应用配置 */
export const APP_CONFIG = {
  /** 应用标题 */
  title: "软件实训教学检查评价与报表系统",

  /** 应用简称 */
  shortName: "B1 实训平台",

  /** 赛事信息 */
  competition: "第十五届中国软件杯（B1）",

  /** API 基础路径 */
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL as string,

  /** 是否启用 Mock */
  useMock: import.meta.env.VITE_USE_MOCK === "true",

  /** Token 存储键 */
  tokenKey: "b1_token",

  /** 用户信息存储键 */
  userInfoKey: "b1_user_info",

  /** 侧边栏折叠状态存储键 */
  sidebarCollapsedKey: "b1_sidebar_collapsed",

  /** 请求超时时间 (ms) */
  requestTimeout: 15000,
} as const
