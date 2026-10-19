package com.b1.module.knowledge.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.common.result.PageResult;
import com.b1.module.admin.entity.ClassInfo;
import com.b1.module.admin.mapper.ClassInfoMapper;
import com.b1.module.course.entity.Course;
import com.b1.module.course.entity.CourseClass;
import com.b1.module.course.entity.CourseTeacher;
import com.b1.module.course.mapper.CourseClassMapper;
import com.b1.module.course.mapper.CourseMapper;
import com.b1.module.course.mapper.CourseTeacherMapper;
import com.b1.module.file.entity.FileStorage;
import com.b1.module.file.mapper.FileStorageMapper;
import com.b1.module.knowledge.dto.KnowledgeDocumentCreateDTO;
import com.b1.module.knowledge.dto.KnowledgeDocumentUpdateDTO;
import com.b1.module.knowledge.entity.KnowledgeDocument;
import com.b1.module.knowledge.mapper.KnowledgeDocumentMapper;
import com.b1.module.knowledge.service.TeacherKnowledgeService;
import com.b1.module.knowledge.vo.KnowledgeDocumentVO;
import com.b1.module.knowledge.vo.KnowledgeVisibilityOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherKnowledgeServiceImpl implements TeacherKnowledgeService {

    private static final Map<String, String> TAG_LABELS = Map.of(
            "grading", "评分细则",
            "template", "提交模板",
            "faq", "FAQ",
            "standard", "规范文档",
            "reference", "参考资料",
            "other", "其他"
    );

    private static final Map<String, String> TAG_COLORS = Map.of(
            "grading", "#1677FF",
            "template", "#722ED1",
            "faq", "#00B96B",
            "standard", "#6b21a8",
            "reference", "#0958D9",
            "other", "#64748b"
    );

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final CourseMapper courseMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseClassMapper courseClassMapper;
    private final ClassInfoMapper classInfoMapper;
    private final FileStorageMapper fileStorageMapper;

    @Override
    public PageResult<KnowledgeDocumentVO> listDocuments(Long courseId, String tag, String keyword,
                                                         int page, int pageSize) {
        Long teacherId = StpUtil.getLoginIdAsLong();
        List<Long> courseIds = getTeacherCourseIds(teacherId);
        if (courseIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page, pageSize, 0);
        }

        LambdaQueryWrapper<KnowledgeDocument> query = new LambdaQueryWrapper<>();
        query.in(KnowledgeDocument::getCourseId, courseIds);
        if (courseId != null) {
            query.eq(KnowledgeDocument::getCourseId, courseId);
        }
        if (StringUtils.hasText(tag) && !"all".equals(tag)) {
            query.eq(KnowledgeDocument::getTag, tag);
        }
        if (StringUtils.hasText(keyword)) {
            query.like(KnowledgeDocument::getTitle, keyword);
        }
        query.orderByDesc(KnowledgeDocument::getCreateTime);

        IPage<KnowledgeDocument> docPage = knowledgeDocumentMapper.selectPage(
                new Page<>(page, pageSize), query);
        List<KnowledgeDocument> records = docPage.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page, pageSize, 0);
        }

        Set<Long> relatedCourseIds = records.stream()
                .map(KnowledgeDocument::getCourseId).collect(Collectors.toSet());
        Set<Long> fileIds = records.stream()
                .map(KnowledgeDocument::getFileStorageId).collect(Collectors.toSet());
        Set<Long> classIds = new HashSet<>();
        for (KnowledgeDocument doc : records) {
            if ("CLASS".equals(doc.getVisibilityScope())) {
                classIds.addAll(parseClassIds(doc.getVisibilityClassIds()));
            }
        }

        Map<Long, Course> courseMap = relatedCourseIds.isEmpty()
                ? Collections.emptyMap()
                : courseMapper.selectBatchIds(relatedCourseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        Map<Long, FileStorage> fileMap = fileIds.isEmpty()
                ? Collections.emptyMap()
                : fileStorageMapper.selectBatchIds(fileIds).stream()
                .collect(Collectors.toMap(FileStorage::getId, Function.identity()));
        Map<Long, ClassInfo> classMap = classIds.isEmpty()
                ? Collections.emptyMap()
                : classInfoMapper.selectBatchIds(classIds).stream()
                .collect(Collectors.toMap(ClassInfo::getId, Function.identity()));

        List<KnowledgeDocumentVO> vos = records.stream()
                .map(doc -> toVO(doc, courseMap, fileMap, classMap))
                .collect(Collectors.toList());

        return PageResult.of(vos, page, pageSize, docPage.getTotal());
    }

    @Override
    public KnowledgeDocumentVO createDocument(KnowledgeDocumentCreateDTO dto) {
        Long teacherId = StpUtil.getLoginIdAsLong();
        assertTeacherOwnsCourse(teacherId, dto.getCourseId());

        FileStorage file = fileStorageMapper.selectById(dto.getFileStorageId());
        if (file == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setCourseId(dto.getCourseId());
        doc.setFileStorageId(dto.getFileStorageId());
        doc.setTitle(dto.getTitle());
        doc.setTag(dto.getTag());
        doc.setVisibilityScope(dto.getVisibilityScope());
        doc.setVisibilityClassIds(joinClassIds(dto.getVisibilityClassIds()));
        doc.setDownloadCount(0);
        doc.setCreateBy(teacherId);

        knowledgeDocumentMapper.insert(doc);
        return toVO(doc, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Override
    public KnowledgeDocumentVO updateDocument(Long documentId, KnowledgeDocumentUpdateDTO dto) {
        KnowledgeDocument doc = knowledgeDocumentMapper.selectById(documentId);
        if (doc == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        assertTeacherOwnsCourse(StpUtil.getLoginIdAsLong(), doc.getCourseId());

        doc.setTitle(dto.getTitle());
        doc.setTag(dto.getTag());
        doc.setVisibilityScope(dto.getVisibilityScope());
        doc.setVisibilityClassIds(joinClassIds(dto.getVisibilityClassIds()));
        if (dto.getFileStorageId() != null) {
            FileStorage file = fileStorageMapper.selectById(dto.getFileStorageId());
            if (file == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND);
            }
            doc.setFileStorageId(dto.getFileStorageId());
        }

        knowledgeDocumentMapper.updateById(doc);
        return toVO(doc, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Override
    public void deleteDocument(Long documentId) {
        KnowledgeDocument doc = knowledgeDocumentMapper.selectById(documentId);
        if (doc == null) {
            return;
        }
        assertTeacherOwnsCourse(StpUtil.getLoginIdAsLong(), doc.getCourseId());
        knowledgeDocumentMapper.deleteById(documentId);
    }

    @Override
    public List<KnowledgeVisibilityOptionVO> getVisibilityOptions(Long courseId) {
        assertTeacherOwnsCourse(StpUtil.getLoginIdAsLong(), courseId);

        List<KnowledgeVisibilityOptionVO> options = new ArrayList<>();
        KnowledgeVisibilityOptionVO courseOption = new KnowledgeVisibilityOptionVO();
        courseOption.setValue("COURSE");
        courseOption.setLabel("本课程全体");
        courseOption.setType("scope");
        options.add(courseOption);

        List<CourseClass> links = courseClassMapper.selectList(
                new LambdaQueryWrapper<CourseClass>().eq(CourseClass::getCourseId, courseId));
        if (!links.isEmpty()) {
            List<Long> classIds = links.stream().map(CourseClass::getClassId).collect(Collectors.toList());
            List<ClassInfo> classes = classInfoMapper.selectBatchIds(classIds);
            for (ClassInfo cls : classes) {
                KnowledgeVisibilityOptionVO vo = new KnowledgeVisibilityOptionVO();
                vo.setValue(String.valueOf(cls.getId()));
                vo.setLabel(cls.getClassName());
                vo.setType("class");
                options.add(vo);
            }
        }
        return options;
    }

    @Override
    public int countByTeacherCourses(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return 0;
        }
        return knowledgeDocumentMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeDocument>()
                        .in(KnowledgeDocument::getCourseId, courseIds)).intValue();
    }

    private void assertTeacherOwnsCourse(Long teacherId, Long courseId) {
        Long count = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getUserId, teacherId)
                        .eq(CourseTeacher::getCourseId, courseId));
        if (count == 0) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }

    private List<Long> getTeacherCourseIds(Long teacherId) {
        return courseTeacherMapper.selectList(
                        new LambdaQueryWrapper<CourseTeacher>()
                                .eq(CourseTeacher::getUserId, teacherId))
                .stream()
                .map(CourseTeacher::getCourseId)
                .distinct()
                .collect(Collectors.toList());
    }

    private KnowledgeDocumentVO toVO(KnowledgeDocument doc,
                                     Map<Long, Course> courseMap,
                                     Map<Long, FileStorage> fileMap,
                                     Map<Long, ClassInfo> classMap) {
        KnowledgeDocumentVO vo = new KnowledgeDocumentVO();
        vo.setId(doc.getId());
        vo.setCourseId(doc.getCourseId());
        vo.setFileStorageId(doc.getFileStorageId());
        vo.setTitle(doc.getTitle());
        vo.setTag(doc.getTag());
        vo.setTagLabel(TAG_LABELS.getOrDefault(doc.getTag(), doc.getTag()));
        vo.setTagColor(TAG_COLORS.getOrDefault(doc.getTag(), "#64748b"));
        vo.setVisibilityScope(doc.getVisibilityScope());
        vo.setVisibilityLabels(resolveVisibilityLabels(doc, classMap));
        vo.setVisibilityClassIds(parseClassIds(doc.getVisibilityClassIds()));
        vo.setDownloadCount(doc.getDownloadCount());
        vo.setUploadedAt(doc.getCreateTime());

        Course course = courseMap.get(doc.getCourseId());
        if (course == null && (courseMap == null || courseMap.isEmpty())) {
            course = courseMapper.selectById(doc.getCourseId());
        }
        if (course != null) {
            vo.setCourseName(course.getCourseName());
        }

        FileStorage file = fileMap.get(doc.getFileStorageId());
        if (file == null && (fileMap == null || fileMap.isEmpty())) {
            file = fileStorageMapper.selectById(doc.getFileStorageId());
        }
        if (file != null) {
            vo.setFileName(file.getOriginalName());
            vo.setFileSize(file.getFileSize());
            vo.setFileSizeDisplay(formatSize(file.getFileSize()));
            vo.setFileType(file.getContentType());
            vo.setDownloadUrl("/api/v1/files/" + file.getId() + "/download");
            vo.setPreviewUrl("/api/v1/files/" + file.getId() + "/preview");
        } else {
            vo.setFileName(doc.getTitle());
            vo.setFileSizeDisplay("-");
        }
        return vo;
    }

    private List<String> resolveVisibilityLabels(KnowledgeDocument doc, Map<Long, ClassInfo> classMap) {
        if ("COURSE".equals(doc.getVisibilityScope())) {
            return List.of("本课程全体");
        }
        if (!StringUtils.hasText(doc.getVisibilityClassIds())) {
            return List.of("本课程全体");
        }
        List<Long> classIds = parseClassIds(doc.getVisibilityClassIds());
        if (classIds.isEmpty()) {
            return List.of("本课程全体");
        }
        if (classMap != null && !classMap.isEmpty()) {
            return classIds.stream()
                    .map(classMap::get)
                    .filter(c -> c != null)
                    .map(ClassInfo::getClassName)
                    .collect(Collectors.toList());
        }
        List<ClassInfo> classes = classInfoMapper.selectBatchIds(classIds);
        return classes.stream()
                .map(ClassInfo::getClassName)
                .collect(Collectors.toList());
    }

    private String joinClassIds(List<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return null;
        }
        return classIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private List<Long> parseClassIds(String classIds) {
        if (!StringUtils.hasText(classIds)) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>();
        for (String part : classIds.split(",")) {
            if (StringUtils.hasText(part)) {
                ids.add(Long.valueOf(part.trim()));
            }
        }
        return ids;
    }

    private String formatSize(Long size) {
        if (size == null || size == 0) {
            return "0 MB";
        }
        if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        }
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }
}
