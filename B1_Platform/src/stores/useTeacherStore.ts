import { defineStore } from "pinia"
import { ref } from "vue"
import type {
  ICourseItem,
  ICourseFormData,
  IStandardItem,
  IDimensionConfig,
  IDimensionItem,
  ITeacherTaskItem,
  ITaskFormData,
  IStudentItem,
  IStudentDetail,
  IPendingSubmission,
  IAIDiagnosis,
  IPublishRequest,
  IStandardTemplate,
  IClassReport,
  ICollegeReport,
  IDashboardData,
} from "@/types/teacher"
import * as teacherApi from "@/api/modules/teacher"

export const useTeacherStore = defineStore("teacher", () => {
  // Courses
  const courses = ref<ICourseItem[]>([])
  const coursesLoading = ref(false)

  // Standards
  const standards = ref<IStandardItem[]>([])
  const standardsLoading = ref(false)
  const currentDimensions = ref<IDimensionConfig | null>(null)
  const isEditingDimensions = ref(false)

  // Tasks
  const tasks = ref<ITeacherTaskItem[]>([])
  const tasksLoading = ref(false)

  // Students
  const students = ref<IStudentItem[]>([])
  const studentsLoading = ref(false)
  const currentStudent = ref<IStudentDetail | null>(null)

  // Submissions
  const pendingSubmissions = ref<IPendingSubmission[]>([])
  const submissionsLoading = ref(false)
  const currentDiagnosis = ref<IAIDiagnosis | null>(null)
  const currentSubmissionId = ref("")

  // Standards Library
  const standardTemplates = ref<IStandardTemplate[]>([])
  const templatesLoading = ref(false)

  // Dashboard
  const dashboardData = ref<IDashboardData | null>(null)
  const dashboardLoading = ref(false)

  // Reports
  const classReport = ref<IClassReport | null>(null)
  const classReportLoading = ref(false)
  const collegeReport = ref<ICollegeReport | null>(null)
  const collegeReportLoading = ref(false)

  // === Courses ===
  async function fetchCourses(): Promise<void> {
    coursesLoading.value = true
    try { courses.value = await teacherApi.fetchCourses() }
    finally { coursesLoading.value = false }
  }

  async function createCourse(data: ICourseFormData): Promise<ICourseItem> {
    const c = await teacherApi.createCourse(data)
    courses.value.push(c)
    return c
  }

  async function updateCourse(courseId: string, data: ICourseFormData): Promise<void> {
    await teacherApi.updateCourse(courseId, data)
    const idx = courses.value.findIndex((c) => c.courseId === courseId)
    if (idx >= 0) courses.value[idx] = { ...courses.value[idx], ...data }
  }

  async function deleteCourse(courseId: string): Promise<void> {
    await teacherApi.deleteCourse(courseId)
    courses.value = courses.value.filter((c) => c.courseId !== courseId)
  }

  // === Standards ===
  async function fetchStandards(): Promise<void> {
    standardsLoading.value = true
    try { standards.value = await teacherApi.fetchStandards() }
    finally { standardsLoading.value = false }
  }

  async function fetchStandardDimensions(standardId: string): Promise<void> {
    currentDimensions.value = await teacherApi.fetchStandardDimensions(standardId)
    isEditingDimensions.value = true
  }

  async function saveDimensions(standardId: string, dimensions: IDimensionItem[]): Promise<void> {
    await teacherApi.updateStandardDimensions(standardId, dimensions)
    if (currentDimensions.value) currentDimensions.value.dimensions = [...dimensions]
    const s = standards.value.find((x) => x.standardId === standardId)
    if (s) s.dimensionCount = dimensions.length
  }

  function resetDimensions(): void {
    currentDimensions.value = null
    isEditingDimensions.value = false
  }

  // === Tasks ===
  async function fetchTasks(params?: { courseId?: string }): Promise<void> {
    tasksLoading.value = true
    try { tasks.value = await teacherApi.fetchTeacherTasks(params) }
    finally { tasksLoading.value = false }
  }

  async function createTask(data: ITaskFormData): Promise<ITeacherTaskItem> {
    const t = await teacherApi.createTask(data)
    tasks.value.push(t)
    return t
  }

  async function updateTask(taskId: string, data: ITaskFormData): Promise<void> {
    await teacherApi.updateTask(taskId, data)
    const idx = tasks.value.findIndex((t) => t.taskId === taskId)
    if (idx >= 0) {
      tasks.value[idx] = { ...tasks.value[idx], ...data, status: tasks.value[idx].status }
    }
  }

  async function deleteTask(taskId: string): Promise<void> {
    await teacherApi.deleteTask(taskId)
    tasks.value = tasks.value.filter((t) => t.taskId !== taskId)
  }

  async function publishTask(taskId: string): Promise<void> {
    await teacherApi.publishTask(taskId)
    const t = tasks.value.find((x) => x.taskId === taskId)
    if (t) t.status = "published"
  }

  // === Students ===
  async function fetchStudents(params?: { className?: string; keyword?: string }): Promise<void> {
    studentsLoading.value = true
    try { students.value = await teacherApi.fetchStudents(params || {}) }
    finally { studentsLoading.value = false }
  }

  async function fetchStudentDetail(userId: string): Promise<void> {
    currentStudent.value = await teacherApi.fetchStudentDetail(userId)
  }

  // === Submissions ===
  async function fetchPendingSubmissions(): Promise<void> {
    submissionsLoading.value = true
    try { pendingSubmissions.value = await teacherApi.fetchPendingSubmissions() }
    finally { submissionsLoading.value = false }
  }

  async function fetchAIDiagnosis(submissionId: string): Promise<void> {
    currentDiagnosis.value = await teacherApi.fetchAIDiagnosis(submissionId)
    currentSubmissionId.value = submissionId
  }

  async function publishReview(submissionId: string, data: IPublishRequest): Promise<void> {
    await teacherApi.publishReview(submissionId, data)
    pendingSubmissions.value = pendingSubmissions.value.filter((s) => s.submissionId !== submissionId)
    currentDiagnosis.value = null
    currentSubmissionId.value = ""
  }

  // === Standards Library ===
  async function fetchStandardTemplates(): Promise<void> {
    templatesLoading.value = true
    try { standardTemplates.value = await teacherApi.fetchStandardTemplates() }
    finally { templatesLoading.value = false }
  }

  // === Dashboard ===
  async function fetchDashboard(): Promise<void> {
    dashboardLoading.value = true
    try { dashboardData.value = await teacherApi.fetchDashboard() }
    finally { dashboardLoading.value = false }
  }

  // === Reports ===
  async function fetchClassReport(className?: string): Promise<void> {
    classReportLoading.value = true
    try { classReport.value = await teacherApi.fetchClassReport({ className }) }
    finally { classReportLoading.value = false }
  }

  async function fetchCollegeReport(): Promise<void> {
    collegeReportLoading.value = true
    try { collegeReport.value = await teacherApi.fetchCollegeReport() }
    finally { collegeReportLoading.value = false }
  }

  // === Reset ===
  function resetStore(): void {
    courses.value = []
    standards.value = []
    tasks.value = []
    students.value = []
    pendingSubmissions.value = []
    dashboardData.value = null
    standardTemplates.value = []
    classReport.value = null
    collegeReport.value = null
    currentDimensions.value = null
    currentStudent.value = null
    currentDiagnosis.value = null
    isEditingDimensions.value = false
  }

  return {
    courses, coursesLoading,
    standards, standardsLoading, currentDimensions, isEditingDimensions,
    tasks, tasksLoading,
    students, studentsLoading, currentStudent,
    pendingSubmissions, submissionsLoading, currentDiagnosis, currentSubmissionId,
    dashboardData, dashboardLoading,
    standardTemplates, templatesLoading,
    classReport, classReportLoading,
    collegeReport, collegeReportLoading,
    fetchDashboard,
    fetchCourses, createCourse, updateCourse, deleteCourse,
    fetchStandards, fetchStandardDimensions, saveDimensions, resetDimensions,
    fetchTasks, createTask, updateTask, deleteTask, publishTask,
    fetchStudents, fetchStudentDetail,
    fetchPendingSubmissions, fetchAIDiagnosis, publishReview,
    fetchStandardTemplates,
    fetchClassReport, fetchCollegeReport,
    resetStore,
  }
})
