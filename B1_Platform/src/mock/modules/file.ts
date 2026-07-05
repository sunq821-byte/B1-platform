import type Mock from "mockjs"

export default function setupFileMock(mock: typeof Mock): void {
  const { Random } = mock

  mock.mock("/api/v1/files/upload", "post", () => {
    return {
      code: 0,
      message: "上传成功",
      data: {
        fileId: `file-${Random.guid()}`,
        fileName: `${Random.word(8, 16)}.zip`,
        fileSize: Random.integer(1024, 52428800),
        fileUrl: `/mock/files/${Random.guid()}.zip`,
      },
      success: true,
      timestamp: Date.now(),
      traceId: Random.guid(),
    }
  })
}
