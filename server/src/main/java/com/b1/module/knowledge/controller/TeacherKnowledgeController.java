package com.b1.module.knowledge.controller;

import com.b1.common.result.PageResult;
import com.b1.common.result.Result;
import com.b1.module.knowledge.dto.KnowledgeDocumentCreateDTO;
import com.b1.module.knowledge.dto.KnowledgeDocumentUpdateDTO;
import com.b1.module.knowledge.service.TeacherKnowledgeService;
import com.b1.module.knowledge.vo.KnowledgeDocumentVO;
import com.b1.module.knowledge.vo.KnowledgeVisibilityOptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "教师-知识库")
@RestController
@RequestMapping("/api/v1/teacher/knowledge")
@RequiredArgsConstructor
public class TeacherKnowledgeController {

    private final TeacherKnowledgeService teacherKnowledgeService;

    @Operation(summary = "资料列表")
    @GetMapping("/documents")
    public PageResult<KnowledgeDocumentVO> listDocuments(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return teacherKnowledgeService.listDocuments(courseId, tag, keyword, page, pageSize);
    }

    @Operation(summary = "上传资料")
    @PostMapping("/documents")
    public Result<KnowledgeDocumentVO> createDocument(@Valid @RequestBody KnowledgeDocumentCreateDTO dto) {
        return Result.ok(teacherKnowledgeService.createDocument(dto));
    }

    @Operation(summary = "更新资料")
    @PutMapping("/documents/{documentId}")
    public Result<KnowledgeDocumentVO> updateDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody KnowledgeDocumentUpdateDTO dto) {
        return Result.ok(teacherKnowledgeService.updateDocument(documentId, dto));
    }

    @Operation(summary = "删除资料")
    @DeleteMapping("/documents/{documentId}")
    public Result<Void> deleteDocument(@PathVariable Long documentId) {
        teacherKnowledgeService.deleteDocument(documentId);
        return Result.ok();
    }

    @Operation(summary = "可见范围选项")
    @GetMapping("/visibility-options")
    public Result<List<KnowledgeVisibilityOptionVO>> getVisibilityOptions(
            @RequestParam Long courseId) {
        return Result.ok(teacherKnowledgeService.getVisibilityOptions(courseId));
    }
}
