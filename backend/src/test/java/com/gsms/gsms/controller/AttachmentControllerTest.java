package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.dto.attachment.AttachmentRenameReq;
import com.gsms.gsms.model.entity.Attachment;
import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.model.entity.ProjectMember;
import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.ProjectMemberRole;
import com.gsms.gsms.model.enums.ProjectStatus;
import com.gsms.gsms.model.enums.ProjectType;
import com.gsms.gsms.model.enums.TaskPriority;
import com.gsms.gsms.model.enums.TaskStatus;
import com.gsms.gsms.model.enums.TaskType;
import com.gsms.gsms.model.enums.TargetType;
import com.gsms.gsms.model.enums.UserStatus;
import com.gsms.gsms.repository.AttachmentMapper;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.infra.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 附件控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class AttachmentControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Autowired
    private AttachmentMapper attachmentMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private Long adminUserId;
    private Long project1Id;
    private Long project2Id;
    private Long task1Id;
    private Long memberUserId;

    @BeforeEach
    void setUp() throws Exception {
        // 创建管理员用户
        User admin = new User();
        admin.setUsername("attachment_admin");
        admin.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi"); // Admin123
        admin.setEmail("attachment_admin@test.com");
        admin.setNickname("附件管理员");
        admin.setStatus(UserStatus.NORMAL);
        admin.setCreateUserId(1L);  // 设置为系统管理员ID
        admin.setUpdateUserId(1L);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        admin.setIsDeleted(0);
        userMapper.insert(admin);
        adminUserId = admin.getId();
        adminToken = jwtUtil.generateToken(admin.getId(), admin.getUsername());

        // 创建普通成员用户
        User member = new User();
        member.setUsername("attachment_member");
        member.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi");
        member.setEmail("attachment_member@test.com");
        member.setNickname("项目成员");
        member.setStatus(UserStatus.NORMAL);
        member.setCreateUserId(1L);
        member.setUpdateUserId(1L);
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        member.setIsDeleted(0);
        userMapper.insert(member);
        memberUserId = member.getId();

        // 使用 executeWithUserContext 创建测试数据
        executeWithUserContext(adminUserId, () -> {
            // 创建项目1
            Project project1 = new Project();
            project1.setName("附件测试项目1");
            project1.setCode("ATTCH01");
            project1.setProjectType(ProjectType.SCHEDULE);
            project1.setDescription("用于测试附件功能的项目");
            project1.setManagerId(adminUserId);
            project1.setStatus(ProjectStatus.NOT_STARTED);
            project1.setPlanStartDate(LocalDate.now());
            project1.setPlanEndDate(LocalDate.now().plusMonths(3));
            project1.setCreateUserId(adminUserId);
            project1.setUpdateUserId(adminUserId);
            project1.setCreateTime(LocalDateTime.now());
            project1.setUpdateTime(LocalDateTime.now());
            project1.setIsDeleted(0);
            projectMapper.insert(project1);
            project1Id = project1.getId();

            // 添加管理员为项目经理
            projectMemberMapper.insertProjectMember(project1Id, adminUserId,
                    ProjectMemberRole.PROJECT_MANAGER.getCode(), adminUserId);

            // 添加普通成员
            projectMemberMapper.insertProjectMember(project1Id, memberUserId,
                    ProjectMemberRole.MEMBER.getCode(), adminUserId);

            // 创建项目2（用于权限测试）
            Project project2 = new Project();
            project2.setName("附件测试项目2");
            project2.setCode("ATTCH02");
            project2.setProjectType(ProjectType.SCHEDULE);
            project2.setDescription("用于权限测试的项目");
            project2.setManagerId(memberUserId);
            project2.setStatus(ProjectStatus.NOT_STARTED);
            project2.setPlanStartDate(LocalDate.now());
            project2.setPlanEndDate(LocalDate.now().plusMonths(3));
            project2.setCreateUserId(memberUserId);
            project2.setUpdateUserId(memberUserId);
            project2.setCreateTime(LocalDateTime.now());
            project2.setUpdateTime(LocalDateTime.now());
            project2.setIsDeleted(0);
            projectMapper.insert(project2);
            project2Id = project2.getId();

            // 添加成员为项目经理
            projectMemberMapper.insertProjectMember(project2Id, memberUserId,
                    ProjectMemberRole.PROJECT_MANAGER.getCode(), memberUserId);

            // 创建任务
            Task task = new Task();
            task.setProjectId(project1Id);
            task.setTitle("附件测试任务");
            task.setDescription("用于测试附件功能的任务");
            task.setType(TaskType.TASK);
            task.setPriority(TaskPriority.MEDIUM);
            task.setAssigneeId(memberUserId);
            task.setStatus(TaskStatus.TODO);
            task.setPlanStartDate(LocalDate.now());
            task.setPlanEndDate(LocalDate.now().plusWeeks(1));
            task.setEstimateHours(new BigDecimal("8.0"));
            task.setCreateUserId(adminUserId);
            task.setUpdateUserId(adminUserId);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            task.setIsDeleted(0);
            taskMapper.insert(task);
            task1Id = task.getId();

            return null;
        });
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        attachmentMapper.selectList(null).forEach(a -> {
            try {
                // 删除物理文件
                Path filePath = Paths.get("./uploads", a.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (Exception e) {
                // 忽略删除错误
            }
        });
    }

    @Test
    void testUploadAttachment_Success() throws Exception {
        // 创建测试文件
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes()
        );

        mockMvc.perform(multipart("/api/attachments/upload")
                        .file(file)
                        .param("targetType", "project")
                        .param("targetId", project1Id.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileName").value("test.txt"))
                .andExpect(jsonPath("$.data.targetType").value("project"))
                .andExpect(jsonPath("$.data.targetId").value(project1Id))
                .andExpect(jsonPath("$.data.uploaderId").value(adminUserId));

        // 验证数据库中存在记录
        executeWithUserContext(adminUserId, () -> {
            java.util.List<Attachment> attachments = attachmentMapper.selectByTarget("project", project1Id);
            assertFalse(attachments.isEmpty());
            assertEquals("test.txt", attachments.get(0).getFileName());
            return null;
        });
    }

    @Test
    void testUploadAttachment_Task() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "task_doc.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        mockMvc.perform(multipart("/api/attachments/upload")
                        .file(file)
                        .param("targetType", "task")
                        .param("targetId", task1Id.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.targetType").value("task"))
                .andExpect(jsonPath("$.data.targetId").value(task1Id));
    }

    @Test
    void testUploadAttachment_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/attachments/upload")
                        .file(file)
                        .param("targetType", "project")
                        .param("targetId", project1Id.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadAttachment_NoPermission() throws Exception {
        // 管理员尝试上传到项目2（不是项目经理也不是创建者）
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "No permission".getBytes()
        );

        mockMvc.perform(multipart("/api/attachments/upload")
                        .file(file)
                        .param("targetType", "project")
                        .param("targetId", project2Id.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void testListAttachments() throws Exception {
        // 先上传一个附件
        executeWithUserContext(adminUserId, () -> {
            Attachment attachment = new Attachment();
            attachment.setFileName("list_test.txt");
            attachment.setDisplayName("列表测试文件");
            attachment.setFilePath("2024/test/list_test.txt");
            attachment.setFileSize(1024L);
            attachment.setFileType("txt");
            attachment.setMimeType("text/plain");
            attachment.setStorageType("local");
            attachment.setTargetType("project");
            attachment.setTargetId(project1Id);
            attachment.setUploaderId(adminUserId);
            attachment.setUploaderName("attachment_admin");
            attachment.setCreateTime(LocalDateTime.now());
            attachment.setUpdateTime(LocalDateTime.now());
            attachment.setIsDeleted(0);
            attachmentMapper.insert(attachment);
            return null;
        });

        mockMvc.perform(get("/api/attachments/list")
                        .param("targetType", "project")
                        .param("targetId", project1Id.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testGetAttachmentDetail() throws Exception {
        // 先创建附件
        Long[] attachmentId = new Long[1];
        executeWithUserContext(adminUserId, () -> {
            Attachment attachment = new Attachment();
            attachment.setFileName("detail_test.txt");
            attachment.setDisplayName("详情测试文件");
            attachment.setFilePath("2024/test/detail_test.txt");
            attachment.setFileSize(2048L);
            attachment.setFileType("txt");
            attachment.setMimeType("text/plain");
            attachment.setStorageType("local");
            attachment.setTargetType("project");
            attachment.setTargetId(project1Id);
            attachment.setUploaderId(adminUserId);
            attachment.setUploaderName("attachment_admin");
            attachment.setCreateTime(LocalDateTime.now());
            attachment.setUpdateTime(LocalDateTime.now());
            attachment.setIsDeleted(0);
            attachmentMapper.insert(attachment);
            attachmentId[0] = attachment.getId();
            return null;
        });

        mockMvc.perform(get("/api/attachments/" + attachmentId[0])
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(attachmentId[0]))
                .andExpect(jsonPath("$.data.fileName").value("detail_test.txt"));
    }

    @Test
    void testRenameAttachment() throws Exception {
        // 先创建附件
        Long[] attachmentId = new Long[1];
        executeWithUserContext(adminUserId, () -> {
            Attachment attachment = new Attachment();
            attachment.setFileName("rename_test.txt");
            attachment.setDisplayName("原名称");
            attachment.setFilePath("2024/test/rename_test.txt");
            attachment.setFileSize(512L);
            attachment.setFileType("txt");
            attachment.setMimeType("text/plain");
            attachment.setStorageType("local");
            attachment.setTargetType("project");
            attachment.setTargetId(project1Id);
            attachment.setUploaderId(adminUserId);
            attachment.setUploaderName("attachment_admin");
            attachment.setCreateTime(LocalDateTime.now());
            attachment.setUpdateTime(LocalDateTime.now());
            attachment.setIsDeleted(0);
            attachmentMapper.insert(attachment);
            attachmentId[0] = attachment.getId();
            return null;
        });

        AttachmentRenameReq req = new AttachmentRenameReq();
        req.setId(attachmentId[0]);
        req.setDisplayName("新名称");

        mockMvc.perform(put("/api/attachments/rename")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证名称已修改
        executeWithUserContext(adminUserId, () -> {
            Attachment attachment = attachmentMapper.selectById(attachmentId[0]);
            assertEquals("新名称", attachment.getDisplayName());
            return null;
        });
    }

    @Test
    void testRenameAttachment_NoPermission() throws Exception {
        // 管理员创建附件，成员尝试重命名
        Long[] attachmentId = new Long[1];
        executeWithUserContext(adminUserId, () -> {
            Attachment attachment = new Attachment();
            attachment.setFileName("admin_file.txt");
            attachment.setDisplayName("管理员文件");
            attachment.setFilePath("2024/test/admin_file.txt");
            attachment.setFileSize(512L);
            attachment.setFileType("txt");
            attachment.setMimeType("text/plain");
            attachment.setStorageType("local");
            attachment.setTargetType("project");
            attachment.setTargetId(project1Id);
            attachment.setUploaderId(adminUserId);
            attachment.setUploaderName("attachment_admin");
            attachment.setCreateTime(LocalDateTime.now());
            attachment.setUpdateTime(LocalDateTime.now());
            attachment.setIsDeleted(0);
            attachmentMapper.insert(attachment);
            attachmentId[0] = attachment.getId();
            return null;
        });

        // 生成成员的 token
        String memberToken = jwtUtil.generateToken(memberUserId, "attachment_member");

        AttachmentRenameReq req = new AttachmentRenameReq();
        req.setId(attachmentId[0]);
        req.setDisplayName("尝试修改");

        mockMvc.perform(put("/api/attachments/rename")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteAttachment() throws Exception {
        // 先创建附件
        Long[] attachmentId = new Long[1];
        executeWithUserContext(adminUserId, () -> {
            Attachment attachment = new Attachment();
            attachment.setFileName("delete_test.txt");
            attachment.setDisplayName("待删除文件");
            attachment.setFilePath("2024/test/delete_test.txt");
            attachment.setFileSize(256L);
            attachment.setFileType("txt");
            attachment.setMimeType("text/plain");
            attachment.setStorageType("local");
            attachment.setTargetType("project");
            attachment.setTargetId(project1Id);
            attachment.setUploaderId(adminUserId);
            attachment.setUploaderName("attachment_admin");
            attachment.setCreateTime(LocalDateTime.now());
            attachment.setUpdateTime(LocalDateTime.now());
            attachment.setIsDeleted(0);
            attachmentMapper.insert(attachment);
            attachmentId[0] = attachment.getId();
            return null;
        });

        mockMvc.perform(delete("/api/attachments/" + attachmentId[0])
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证已逻辑删除
        executeWithUserContext(adminUserId, () -> {
            Attachment attachment = attachmentMapper.selectById(attachmentId[0]);
            assertNull(attachment); // 逻辑删除后查询不到
            return null;
        });
    }

    @Test
    void testGetAttachmentByMember() throws Exception {
        // 成员用户可以查看项目附件
        String memberToken = jwtUtil.generateToken(memberUserId, "attachment_member");

        executeWithUserContext(adminUserId, () -> {
            Attachment attachment = new Attachment();
            attachment.setFileName("member_view_test.txt");
            attachment.setDisplayName("成员可查看文件");
            attachment.setFilePath("2024/test/member_view_test.txt");
            attachment.setFileSize(128L);
            attachment.setFileType("txt");
            attachment.setMimeType("text/plain");
            attachment.setStorageType("local");
            attachment.setTargetType("project");
            attachment.setTargetId(project1Id);
            attachment.setUploaderId(adminUserId);
            attachment.setUploaderName("attachment_admin");
            attachment.setCreateTime(LocalDateTime.now());
            attachment.setUpdateTime(LocalDateTime.now());
            attachment.setIsDeleted(0);
            attachmentMapper.insert(attachment);
            return null;
        });

        mockMvc.perform(get("/api/attachments/list")
                        .param("targetType", "project")
                        .param("targetId", project1Id.toString())
                        .header("Authorization", "Bearer " + memberToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }
}
