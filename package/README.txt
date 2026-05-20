ClassNote - 本地笔记管理应用
===============================

使用方法：
1. 确保已安装 Java 17 或更高版本
2. 双击 start.bat 启动
3. 浏览器打开 http://localhost:8080

数据位置：
- 数据库: %USERPROFILE%\.classnote
- 附件: %USERPROFILE%\classnote-files

如需使用 MySQL（多用户场景）：
  java -jar ClassNote.jar --spring.profiles.active=mysql
  并配置 src/main/resources/application.yml 中的数据库连接信息

功能：
- 笔记本/笔记管理（Markdown 编辑 + 实时预览）
- 标签管理（颜色标识、按标签筛选）
- 全文搜索（关键词高亮）
- 附件上传（图片/文件）
- 数据导出（Markdown 文件）
- 项目分析（接入大模型 API，生成知识点）
- 阶段总结（选中多篇笔记，AI 自动归纳）
- 深色/浅色模式
- 草稿自动保存
