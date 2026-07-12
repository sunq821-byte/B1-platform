#!/usr/bin/env python3
"""Generate an idempotent demo-seed SQL for the B1 platform.

Seeds one class (~30 students) enrolled in course 1 (Java Web 开发), with
submissions across task 1 (Spring Boot, max 100) and task 2 (Servlet, max 80),
each carrying a COMPLETED AI analysis (per-dimension detail) and, for graded
ones, a teacher review + published score_record.

Status mix is deliberate: the teacher dashboard, student growth center and admin
overview read DISJOINT submission.status vocabularies, so no single status can
satisfy all three. See the per-task buckets below.

Reproducible: fixed RNG seed; fixed ID band [3_000_000_000, 3_000_999_999] so the
script can DELETE-then-INSERT and be re-run / rolled back cleanly.
"""
import random
from datetime import date, timedelta

random.seed(20260712)

HASH = '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva'  # bcrypt('123456')
COURSE_ID = 1
CLASS_ID = 2074718636818112514   # 软件工程24402
TEACHER_ID = 2                   # teacher1 张教授 (on course 1 via course_teacher)

DIMS = [  # (id, name, max_score) for standard_id=1, sums to 100
    (1, '代码规范', 25),
    (2, '功能完成度', 30),
    (3, '创新设计', 20),
    (4, '文档撰写', 15),
    (5, 'Git规范', 10),
]

BAND = 3_000_000_000
U_BASE   = BAND + 0          # users / user_role / course_student idx 1..30
SUB_BASE = {1: BAND + 10_000, 2: BAND + 20_000}
AIA_BASE = {1: BAND + 110_000, 2: BAND + 120_000}
AID_BASE = BAND + 200_000     # running counter
REV_BASE = {1: BAND + 310_000, 2: BAND + 320_000}
SR_BASE  = {1: BAND + 410_000, 2: BAND + 420_000}

NAMES = [
    '王浩然', '李思远', '张梦琪', '刘子墨', '陈嘉懿', '杨雨桐', '赵梓涵', '黄俊杰',
    '周欣妍', '吴宇轩', '徐若曦', '孙铭泽', '胡佳宁', '朱瑞霖', '高晨曦', '林书瑶',
    '何俊熙', '郭子恒', '罗诗涵', '宋昊天', '谢雨墨', '唐嘉悦', '韩沐宸', '冯艺涵',
    '董浩宇', '萧亦辰', '程语桐', '曹梓睿', '彭思彤', '潘展鹏',
]

TASKS = {
    1: dict(max=100, mean=78, sd=11, lo=45, hi=98, base=date(2026, 5, 6),  spread=18),
    2: dict(max=80,  mean=60, sd=9,  lo=38, hi=78, base=date(2026, 6, 16), spread=20),
}

COMMENTS = [
    '整体完成度不错，核心功能实现完整，建议加强异常处理与边界校验。',
    '代码结构清晰，命名规范到位；文档略显单薄，可补充接口说明。',
    '功能基本实现，但存在硬编码与重复代码，建议抽取公共方法。',
    '设计有一定亮点，分层合理；Git 提交记录偏少，建议规范提交粒度。',
    '完成质量较高，注释充分，可读性好，继续保持。',
    '需求实现存在遗漏，部分接口未联调，建议对照任务书自查。',
    '代码规范良好，安全性需注意，避免 SQL 拼接与明文密码。',
    '文档完整、结构规范，功能演示流畅，综合表现优秀。',
]

REASONS = {
    '代码规范': ['命名不统一、缺少必要注释', '存在魔法数字与超长方法', '风格良好，个别处可优化'],
    '功能完成度': ['核心流程已实现，边界场景缺失', '需求实现完整、演示正常', '部分功能未完成'],
    '创新设计': ['架构分层合理，存在优化空间', '缺少设计亮点与抽象', '有一定创新点'],
    '文档撰写': ['README 不完整，缺接口文档', '文档结构清晰、内容充分', '缺少设计说明'],
    'Git规范': ['提交信息不规范、粒度过大', '分支与提交记录规范', '提交记录偏少'],
}
SUGGESTS = {
    '代码规范': '统一命名、补充注释、消除魔法数字。',
    '功能完成度': '对照任务书补齐缺失功能并完善边界处理。',
    '创新设计': '引入合理分层与抽象，提炼可复用组件。',
    '文档撰写': '补充 README、接口文档与设计说明。',
    'Git规范': '规范提交信息，控制提交粒度，善用分支。',
}


def dim_scores(total, task_max):
    """Split total across dims proportional to dim max, with noise, summing to total."""
    scores = []
    for _, _, dmax in DIMS:
        ideal = total * dmax / 100.0
        s = round(ideal + random.uniform(-1.5, 1.5))
        s = max(0, min(dmax, s))
        scores.append(s)
    # fix drift so sum == total (respecting per-dim caps)
    drift = total - sum(scores)
    order = sorted(range(len(DIMS)), key=lambda i: -DIMS[i][2])
    guard = 0
    while drift != 0 and guard < 200:
        for i in order:
            if drift == 0:
                break
            dmax = DIMS[i][2]
            if drift > 0 and scores[i] < dmax:
                scores[i] += 1
                drift -= 1
            elif drift < 0 and scores[i] > 0:
                scores[i] -= 1
                drift += 1
        guard += 1
    return scores


def gauss_total(t):
    v = round(random.gauss(t['mean'], t['sd']))
    return max(t['lo'], min(t['hi'], v))


def dt(d, hh, mm):
    return f"{d.isoformat()} {hh:02d}:{mm:02d}:00"


def esc(s):
    return s.replace("'", "''")


out = []
w = out.append

w("-- ============================================================")
w("-- B1 演示数据 seed（幂等，可重复运行 / 可回滚）")
w("-- 生成器: server/db/demo/gen_seed_demo.py  (勿手改本文件)")
w(f"-- ID 段: [{BAND}, {BAND+999999}]  回滚见文件末尾 ROLLBACK 段")
w("-- ============================================================")
w("SET NAMES utf8mb4;")
w("START TRANSACTION;")
w("")
w("-- 清理旧的 seed（按 ID 段），保证可重复运行")
w(f"DELETE FROM ai_analysis_detail WHERE id BETWEEN {BAND} AND {BAND+999999};")
w(f"DELETE FROM score_record       WHERE id BETWEEN {BAND} AND {BAND+999999};")
w(f"DELETE FROM teacher_review     WHERE id BETWEEN {BAND} AND {BAND+999999};")
w(f"DELETE FROM ai_analysis        WHERE id BETWEEN {BAND} AND {BAND+999999};")
w(f"DELETE FROM submission         WHERE id BETWEEN {BAND} AND {BAND+999999};")
w(f"DELETE FROM course_student     WHERE id BETWEEN {BAND} AND {BAND+999999};")
w(f"DELETE FROM user_role          WHERE id BETWEEN {BAND} AND {BAND+999999};")
w(f"DELETE FROM user               WHERE id BETWEEN {BAND} AND {BAND+999999};")
w("")

# ---- users / roles / enrollment ----
w("-- 30 名学生 + 角色 + 选课")
for i, name in enumerate(NAMES, start=1):
    uid = U_BASE + i
    username = f"2024{i:06d}"[-10:]
    stu_no = f"2024010{i:03d}"
    email = f"stu{i:03d}@b1.edu.cn"
    phone = f"139{i:08d}"[:11]
    w(f"INSERT INTO user (id,username,password,real_name,email,phone,status,deleted,create_time) "
      f"VALUES ({uid},'{stu_no}','{HASH}','{esc(name)}','{email}','{phone}',1,0,NOW());")
for i in range(1, len(NAMES) + 1):
    rid = U_BASE + i
    w(f"INSERT INTO user_role (id,user_id,role_id) VALUES ({rid},{U_BASE+i},3);")
for i in range(1, len(NAMES) + 1):
    csid = U_BASE + i
    w(f"INSERT INTO course_student (id,course_id,user_id,class_id) "
      f"VALUES ({csid},{COURSE_ID},{U_BASE+i},{CLASS_ID});")
w("")

aid_counter = AID_BASE


def emit_ai(sub_id, task_id, total, task_max, st_dt):
    """Emit ai_analysis (COMPLETED) + 5 detail rows. Returns nothing."""
    global aid_counter
    aia_id = AIA_BASE[task_id] + idx
    scores = dim_scores(total, task_max)
    ms = random.randint(6000, 13000)
    tin, tout = random.randint(1800, 3200), random.randint(600, 1400)
    comp_dt = st_dt  # keep same day; minutes offset not important
    w(f"INSERT INTO ai_analysis (id,submission_id,total_score,analysis_status,analysis_time_ms,"
      f"model_provider,model_name,token_input,token_output,token_total,retry_count,raw_response,"
      f"start_time,complete_time,version,create_time) VALUES "
      f"({aia_id},{sub_id},{total}.00,'COMPLETED',{ms},'DEEPSEEK+QWEN','deepseek-chat',"
      f"{tin},{tout},{tin+tout},0,JSON_OBJECT('seeded',true),'{st_dt}','{comp_dt}',0,NOW());")
    for di, (dim_id, dim_name, dmax) in enumerate(DIMS):
        sc = scores[di]
        deduct = dmax - sc
        sev = 'INFO' if deduct <= 2 else ('MINOR' if deduct <= 6 else 'MAJOR')
        reason = random.choice(REASONS[dim_name])
        sug = SUGGESTS[dim_name]
        conf = round(random.uniform(0.72, 0.95), 2)
        w(f"INSERT INTO ai_analysis_detail (id,ai_analysis_id,dimension_id,agent_type,file_path,"
          f"issue_type,severity,reason,suggestion,suggest_deduct,confidence,sort_order,create_time) "
          f"VALUES ({aid_counter},{aia_id},{dim_id},'DEEPSEEK','src/Main.java','CODE_ISSUE',"
          f"'{sev}','{esc(reason)}','{esc(sug)}',{deduct}.00,{conf},{di+1},NOW());")
        aid_counter += 1
    return total


def emit_review(sub_id, task_id, rv_dt, pub_dt, rejected=False):
    rid = REV_BASE[task_id] + idx
    comment = random.choice(COMMENTS)
    w(f"INSERT INTO teacher_review (id,submission_id,reviewer_id,teacher_comment,status,"
      f"review_time,publish_time,version,create_time) VALUES "
      f"({rid},{sub_id},{TEACHER_ID},'{esc(comment)}','PUBLISHED','{rv_dt}','{pub_dt}',0,NOW());")


def emit_score(sub_id, task_id, user_id, ai_total, task_max, pub_dt):
    srid = SR_BASE[task_id] + idx
    adjust = random.randint(-3, 5)
    final = max(0, min(task_max, ai_total + adjust))
    w(f"INSERT INTO score_record (id,submission_id,user_id,training_task_id,total_score,"
      f"ai_total_score,teacher_comment,status,publish_time,version,create_time) VALUES "
      f"({srid},{sub_id},{user_id},{task_id},{final}.00,{ai_total}.00,NULL,'PUBLISHED','{pub_dt}',0,NOW());")


def buckets(task_id):
    """Return dict idx->status-plan for a task."""
    plan = {}
    if task_id == 1:
        for i in range(1, 25):   plan[i] = 'reviewed'   # REVIEWED + review + score
        for i in range(25, 28):  plan[i] = 'pending'    # SUBMITTED, ai only
        for i in range(28, 30):  plan[i] = 'completed'  # status COMPLETED (admin) + review + score
        plan[30] = 'rejected'                            # REJECTED, review, no score
    else:  # task 2: only 26 of 30 submit
        for i in range(1, 21):   plan[i] = 'sub_reviewed'  # SUBMITTED + review + score (dashboard "reviewed")
        for i in range(21, 25):  plan[i] = 'pending'
        for i in range(25, 27):  plan[i] = 'reviewed'
        # 27..30: no submission
    return plan


for task_id, t in TASKS.items():
    w(f"-- 任务 {task_id} 提交/AI/复核/成绩")
    plan = buckets(task_id)
    for idx in range(1, len(NAMES) + 1):
        if idx not in plan:
            continue
        status_plan = plan[idx]
        user_id = U_BASE + idx
        sub_id = SUB_BASE[task_id] + idx
        sub_day = t['base'] + timedelta(days=(idx % t['spread']))
        rev_day = sub_day + timedelta(days=2)
        st_dt = dt(sub_day, 9 + (idx % 8), (idx * 7) % 60)
        rv_dt = dt(rev_day, 14, (idx * 5) % 60)
        pub_dt = dt(rev_day, 15, (idx * 5) % 60)
        is_late = 1 if (task_id == 1 and idx in (11, 23)) else 0

        sub_status = {
            'reviewed': 'REVIEWED',
            'pending': 'SUBMITTED',
            'completed': 'COMPLETED',
            'rejected': 'REJECTED',
            'sub_reviewed': 'SUBMITTED',
        }[status_plan]

        w(f"INSERT INTO submission (id,training_task_id,user_id,submit_type,summary,submit_count,"
          f"submit_time,is_late,status,deleted,version,create_time) VALUES "
          f"({sub_id},{task_id},{user_id},'ZIP_UPLOAD','实训成果提交',1,'{st_dt}',{is_late},"
          f"'{sub_status}',0,0,NOW());")

        total = gauss_total(t)
        emit_ai(sub_id, task_id, total, t['max'], st_dt)

        if status_plan in ('reviewed', 'completed', 'sub_reviewed'):
            emit_review(sub_id, task_id, rv_dt, pub_dt)
            emit_score(sub_id, task_id, user_id, total, t['max'], pub_dt)
        elif status_plan == 'rejected':
            emit_review(sub_id, task_id, rv_dt, pub_dt, rejected=True)
        # 'pending': AI only, no review/score
    w("")

w("COMMIT;")
w("")
w("-- ============================================================")
w("-- ROLLBACK（如需清除本 seed，执行下列语句）")
w("-- START TRANSACTION;")
for tbl in ['ai_analysis_detail', 'score_record', 'teacher_review', 'ai_analysis',
            'submission', 'course_student', 'user_role', 'user']:
    w(f"-- DELETE FROM {tbl} WHERE id BETWEEN {BAND} AND {BAND+999999};")
w("-- COMMIT;")
w("-- ============================================================")

sql = "\n".join(out) + "\n"
with open("D:/Project/B1/server/db/demo/seed_demo.sql", "w", encoding="utf-8") as f:
    f.write(sql)
print(f"wrote seed_demo.sql: {len(out)} lines")
