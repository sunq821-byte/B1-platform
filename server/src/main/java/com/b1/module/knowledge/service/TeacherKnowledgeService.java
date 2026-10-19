package com.b1.module.knowledge.service;

import com.b1.common.result.PageResult;
import com.b1.module.knowledge.dto.KnowledgeDocumentCreateDTO;
import com.b1.module.knowledge.dto.KnowledgeDocumentUpdateDTO;
import com.b1.module.knowledge.vo.KnowledgeDocumentVO;
import com.b1.module.knowledge.vo.KnowledgeVisibilityOptionVO;

import java.util.List;

public interface TeacherKnowledgeService {

    PageResult<KnowledgeDocumentVO> listDocuments(Long courseId, String tag, String keyword,
                                                  int page, int pageSize);

    KnowledgeDocumentVO createDocument(KnowledgeDocumentCreateDTO dto);

    KnowledgeDocumentVO updateDocument(Long documentId, KnowledgeDocumentUpdateDTO dto);

    void deleteDocument(Long documentId);

    List<KnowledgeVisibilityOptionVO> getVisibilityOptions(Long courseId);

    int countByTeacherCourses(List<Long> courseIds);
}
