package com.b1.module.teacher.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.common.result.PageResult;
import com.b1.module.auth.entity.User;
import com.b1.module.auth.mapper.UserMapper;
import com.b1.module.course.entity.Course;
import com.b1.module.course.entity.CourseStudent;
import com.b1.module.course.entity.CourseTeacher;
import com.b1.module.course.mapper.CourseMapper;
import com.b1.module.course.mapper.CourseStudentMapper;
import com.b1.module.course.mapper.CourseTeacherMapper;
import com.b1.module.review.entity.TeacherReview;
import com.b1.module.review.mapper.TeacherReviewMapper;
import com.b1.module.score.entity.ScoreRecord;
import com.b1.module.score.mapper.ScoreRecordMapper;
import com.b1.module.standard.entity.EvaluationStandard;
import com.b1.module.standard.entity.StandardDimension;
import com.b1.module.standard.mapper.EvaluationStandardMapper;
import com.b1.module.standard.mapper.StandardDimensionMapper;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import com.b1.module.teacher.dto.StandardCreateDTO;
import com.b1.module.teacher.dto.StandardUpdateDTO;
import com.b1.module.teacher.service.TeacherService;
import com.b1.module.teacher.vo.StandardDetailVO;
import com.b1.module.teacher.vo.StandardListVO;
import com.b1.module.teacher.vo.TeacherDashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseMapper courseMapper;
    private final CourseStudentMapper courseStudentMapper;
    private final TrainingTaskMapper trainingTaskMapper;
    private final SubmissionMapper submissionMapper;
    private final TeacherReviewMapper teacherReviewMapper;
    private final ScoreRecordMapper scoreRecordMapper;
    private final UserMapper userMapper;
    private final EvaluationStandardMapper evaluationStandardMapper;
    private final StandardDimensionMapper standardDimensionMapper;

    @Override
    public TeacherDashboardVO getDashboard() {
        Long teacherId = StpUtil.getLoginIdAsLong();

        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getUserId, teacherId));

        TeacherDashboardVO vo = new TeacherDashboardVO();
        TeacherDashboardVO.DashboardStatsVO stats = new TeacherDashboardVO.DashboardStatsVO();
        TeacherDashboardVO.SubmitRateVO submitRate = new TeacherDashboardVO.SubmitRateVO();
        vo.setStats(stats);
        vo.setSubmitRateByClass(submitRate);

        if (courseTeachers.isEmpty()) {
            stats.setTotalStudents(0);
            stats.setClassCount(0);
            stats.setPendingCount(0);
            stats.setReviewedCount(0);
            stats.setSubmissionRate(0);
            vo.setPendingReviews(Collections.emptyList());
            submitRate.setClassNames(Collections.emptyList());
            submitRate.setValues(Collections.emptyList());
            return vo;
        }

        List<Long> courseIds = courseTeachers.stream()
                .map(CourseTeacher::getCourseId).distinct().toList();
        stats.setClassCount(courseIds.size());

        List<CourseStudent> allStudents = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>()
                        .in(CourseStudent::getCourseId, courseIds));
        long distinctStudents = allStudents.stream()
                .map(CourseStudent::getUserId).distinct().count();
        stats.setTotalStudents((int) distinctStudents);

        List<TrainingTask> allTasks = trainingTaskMapper.selectList(
                new LambdaQueryWrapper<TrainingTask>()
                        .in(TrainingTask::getCourseId, courseIds)
                        .eq(TrainingTask::getDeleted, 0));

        List<Long> taskIds = allTasks.stream().map(TrainingTask::getId).toList();
        int pendingReview = 0;
        long reviewedCount = 0;
        if (!taskIds.isEmpty()) {
            List<Submission> submittedSubs = submissionMapper.selectList(
                    new LambdaQueryWrapper<Submission>()
                            .in(Submission::getTrainingTaskId, taskIds)
                            .eq(Submission::getStatus, "SUBMITTED")
                            .eq(Submission::getDeleted, 0));

            if (!submittedSubs.isEmpty()) {
                List<Long> submittedIds = submittedSubs.stream()
                        .map(Submission::getId).toList();
                Set<Long> reviewedIds = teacherReviewMapper.selectList(
                        new LambdaQueryWrapper<TeacherReview>()
                                .in(TeacherReview::getSubmissionId, submittedIds))
                        .stream().map(TeacherReview::getSubmissionId)
                        .collect(Collectors.toSet());
                pendingReview = (int) submittedSubs.stream()
                        .filter(s -> !reviewedIds.contains(s.getId())).count();
                reviewedCount = reviewedIds.size();
            }
        }
        stats.setPendingCount(pendingReview);
        stats.setReviewedCount((int) reviewedCount);

        // submission rate: if total tasks > 0, calculate percentage
        if (!allTasks.isEmpty() && distinctStudents > 0) {
            long totalPossible = (long) allTasks.size() * distinctStudents;
            long totalSubmissions = pendingReview + (int) reviewedCount;
            int rate = totalPossible > 0
                    ? (int) ((totalSubmissions * 100) / totalPossible)
                    : 0;
            stats.setSubmissionRate(rate);
        } else {
            stats.setSubmissionRate(0);
        }

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c, (a, b) -> a));

        // submit rate by class
        List<String> classNames = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (Long courseId : courseIds) {
            Course course = courseMap.get(courseId);
            String name = course != null ? course.getCourseName() : "";
            classNames.add(name);

            List<Long> courseTaskIds = allTasks.stream()
                    .filter(t -> t.getCourseId().equals(courseId))
                    .map(TrainingTask::getId).toList();
            long courseStuCount = allStudents.stream()
                    .filter(s -> s.getCourseId().equals(courseId)).count();
            int classRate = 0;
            if (!courseTaskIds.isEmpty() && courseStuCount > 0) {
                List<Submission> courseSubs = submissionMapper.selectList(
                        new LambdaQueryWrapper<Submission>()
                                .in(Submission::getTrainingTaskId, courseTaskIds)
                                .eq(Submission::getDeleted, 0));
                long totalPossible = courseTaskIds.size() * courseStuCount;
                classRate = totalPossible > 0
                        ? (int) ((courseSubs.size() * 100) / totalPossible)
                        : 0;
            }
            values.add(classRate);
        }
        submitRate.setClassNames(classNames);
        submitRate.setValues(values);

        // pending reviews
        List<Submission> recentSubs = new ArrayList<>();
        if (!taskIds.isEmpty()) {
            recentSubs = submissionMapper.selectList(
                    new LambdaQueryWrapper<Submission>()
                            .in(Submission::getTrainingTaskId, taskIds)
                            .eq(Submission::getDeleted, 0)
                            .orderByDesc(Submission::getSubmitTime)
                            .last("LIMIT 10"));
        }

        Set<Long> recentUserIds = recentSubs.stream()
                .map(Submission::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = recentUserIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(new ArrayList<>(recentUserIds)).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        Map<Long, TrainingTask> taskMap = allTasks.stream()
                .collect(Collectors.toMap(TrainingTask::getId, t -> t, (a, b) -> a));

        List<TeacherDashboardVO.PendingReviewVO> pendingVOs = new ArrayList<>();
        for (Submission s : recentSubs) {
            TeacherDashboardVO.PendingReviewVO pr = new TeacherDashboardVO.PendingReviewVO();
            pr.setSubmissionId(s.getId());
            pr.setStatus(s.getStatus());
            pr.setSubmittedAt(s.getSubmitTime());

            User u = userMap.get(s.getUserId());
            pr.setStudentName(u != null ? u.getRealName() : "");

            TrainingTask t = taskMap.get(s.getTrainingTaskId());
            if (t != null) {
                pr.setTaskName(t.getTaskName());
            }
            pendingVOs.add(pr);
        }
        vo.setPendingReviews(pendingVOs);

        return vo;
    }

    @Override
    public PageResult<StandardListVO> listStandards(int page, int pageSize, String keyword, Integer isTemplate) {
        LambdaQueryWrapper<EvaluationStandard> query = new LambdaQueryWrapper<EvaluationStandard>()
                .eq(EvaluationStandard::getDeleted, 0);
        if (isTemplate != null) {
            query.eq(EvaluationStandard::getIsTemplate, isTemplate);
        }
        if (StringUtils.hasText(keyword)) {
            query.like(EvaluationStandard::getStandardName, keyword);
        }
        query.orderByDesc(EvaluationStandard::getCreateTime);

        IPage<EvaluationStandard> standardPage = evaluationStandardMapper.selectPage(
                new Page<>(page, pageSize), query);

        List<StandardListVO> vos = new ArrayList<>();
        for (EvaluationStandard es : standardPage.getRecords()) {
            StandardListVO vo = new StandardListVO();
            vo.setStandardId(es.getId());
            vo.setStandardName(es.getStandardName());
            vo.setDescription(es.getDescription());
            vo.setCourseType(es.getCourseType());
            vo.setStatus(es.getStatus());
            vo.setIsTemplate(es.getIsTemplate());
            vo.setCreateTime(es.getCreateTime());

            Long dimCount = standardDimensionMapper.selectCount(
                    new LambdaQueryWrapper<StandardDimension>()
                            .eq(StandardDimension::getStandardId, es.getId()));
            vo.setDimensionCount(dimCount.intValue());

            vos.add(vo);
        }

        return PageResult.of(vos, page, pageSize, standardPage.getTotal());
    }

    @Override
    public StandardDetailVO getStandardDetail(Long standardId) {
        EvaluationStandard es = evaluationStandardMapper.selectById(standardId);
        if (es == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价标准不存在");
        }

        StandardDetailVO vo = new StandardDetailVO();
        vo.setStandardId(es.getId());
        vo.setStandardName(es.getStandardName());
        vo.setDescription(es.getDescription());
        vo.setCourseType(es.getCourseType());
        vo.setStatus(es.getStatus());
        vo.setIsTemplate(es.getIsTemplate());
        vo.setCreateTime(es.getCreateTime());

        List<StandardDimension> dims = standardDimensionMapper.selectList(
                new LambdaQueryWrapper<StandardDimension>()
                        .eq(StandardDimension::getStandardId, standardId)
                        .orderByAsc(StandardDimension::getSortOrder));

        List<StandardDetailVO.DimensionVO> dimVOs = new ArrayList<>();
        for (StandardDimension dim : dims) {
            StandardDetailVO.DimensionVO dv = new StandardDetailVO.DimensionVO();
            dv.setDimensionId(dim.getId());
            dv.setDimName(dim.getDimName());
            dv.setDimDescription(dim.getDimDescription());
            dv.setWeight(dim.getWeight());
            dv.setMaxScore(dim.getMaxScore());
            dv.setSortOrder(dim.getSortOrder());
            dimVOs.add(dv);
        }
        vo.setDimensions(dimVOs);

        return vo;
    }

    @Override
    public StandardListVO createStandard(StandardCreateDTO dto) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        EvaluationStandard es = new EvaluationStandard();
        es.setStandardName(dto.getStandardName());
        es.setDescription(dto.getDescription());
        es.setCourseType(dto.getCourseType());
        es.setIsTemplate(dto.getIsTemplate() != null ? dto.getIsTemplate() : 0);
        es.setStatus("PUBLISHED");

        evaluationStandardMapper.insert(es);

        int sortOrder = 1;
        for (StandardCreateDTO.DimensionItem item : dto.getDimensions()) {
            StandardDimension dim = new StandardDimension();
            dim.setStandardId(es.getId());
            dim.setDimName(item.getDimName());
            dim.setDimDescription(item.getDimDescription());
            dim.setWeight(item.getWeight());
            dim.setMaxScore(item.getMaxScore());
            dim.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : sortOrder);
            standardDimensionMapper.insert(dim);
            sortOrder++;
        }

        StandardListVO vo = new StandardListVO();
        vo.setStandardId(es.getId());
        vo.setStandardName(es.getStandardName());
        vo.setDescription(es.getDescription());
        vo.setCourseType(es.getCourseType());
        vo.setStatus(es.getStatus());
        vo.setIsTemplate(es.getIsTemplate());
        vo.setDimensionCount(dto.getDimensions().size());
        vo.setCreateTime(es.getCreateTime());

        return vo;
    }

    @Override
    public StandardListVO updateStandard(Long standardId, StandardUpdateDTO dto) {
        EvaluationStandard es = evaluationStandardMapper.selectById(standardId);
        if (es == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价标准不存在");
        }

        if (StringUtils.hasText(dto.getStandardName())) {
            es.setStandardName(dto.getStandardName());
        }
        if (StringUtils.hasText(dto.getDescription())) {
            es.setDescription(dto.getDescription());
        }
        if (StringUtils.hasText(dto.getCourseType())) {
            es.setCourseType(dto.getCourseType());
        }

        evaluationStandardMapper.updateById(es);

        EvaluationStandard updated = evaluationStandardMapper.selectById(standardId);

        Long dimCount = standardDimensionMapper.selectCount(
                new LambdaQueryWrapper<StandardDimension>()
                        .eq(StandardDimension::getStandardId, standardId));

        StandardListVO vo = new StandardListVO();
        vo.setStandardId(updated.getId());
        vo.setStandardName(updated.getStandardName());
        vo.setDescription(updated.getDescription());
        vo.setCourseType(updated.getCourseType());
        vo.setStatus(updated.getStatus());
        vo.setIsTemplate(updated.getIsTemplate());
        vo.setDimensionCount(dimCount.intValue());
        vo.setCreateTime(updated.getCreateTime());

        return vo;
    }

    @Override
    public StandardListVO copyStandard(Long standardId) {
        EvaluationStandard source = evaluationStandardMapper.selectById(standardId);
        if (source == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价标准不存在");
        }

        EvaluationStandard copy = new EvaluationStandard();
        copy.setStandardName(source.getStandardName() + " (副本)");
        copy.setDescription(source.getDescription());
        copy.setCourseType(source.getCourseType());
        copy.setIsTemplate(0);
        copy.setStatus("PUBLISHED");

        evaluationStandardMapper.insert(copy);

        List<StandardDimension> sourceDims = standardDimensionMapper.selectList(
                new LambdaQueryWrapper<StandardDimension>()
                        .eq(StandardDimension::getStandardId, standardId)
                        .orderByAsc(StandardDimension::getSortOrder));

        for (StandardDimension dim : sourceDims) {
            StandardDimension newDim = new StandardDimension();
            newDim.setStandardId(copy.getId());
            newDim.setDimName(dim.getDimName());
            newDim.setDimDescription(dim.getDimDescription());
            newDim.setWeight(dim.getWeight());
            newDim.setMaxScore(dim.getMaxScore());
            newDim.setSortOrder(dim.getSortOrder());
            standardDimensionMapper.insert(newDim);
        }

        StandardListVO vo = new StandardListVO();
        vo.setStandardId(copy.getId());
        vo.setStandardName(copy.getStandardName());
        vo.setDescription(copy.getDescription());
        vo.setCourseType(copy.getCourseType());
        vo.setStatus(copy.getStatus());
        vo.setIsTemplate(copy.getIsTemplate());
        vo.setDimensionCount(sourceDims.size());
        vo.setCreateTime(copy.getCreateTime());

        return vo;
    }
}
