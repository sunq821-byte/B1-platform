/** 统一 API 响应格式 */
export interface ApiResponse<T = unknown> {
  code: number
  data: T
  message: string
  traceId?: string
  requestId?: string
  path?: string
}

/** 分页请求参数 */
export interface PageParams {
  page: number
  pageSize: number
  sortField?: string
  sortOrder?: "asc" | "desc"
  keyword?: string
  status?: string
}

/** 分页响应 */
export interface PageResult<T> {
  list: T[]
  page: number
  pageSize: number
  total: number
  totalPages: number
}
