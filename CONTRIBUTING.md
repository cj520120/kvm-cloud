中文 | [English](CONTRIBUTING-en.md)

# KVM Cloud 贡献指南

感谢你关注并参与 KVM Cloud 的建设。本指南说明如何报告问题、提出改进建议、搭建开发环境，以及向开源仓库提交变更。

KVM Cloud 是一个基于 Java 8 和 Spring Boot 的项目，包含三个 Maven 模块：

- `cloud-common`：公共模型、工具类以及 libvirt/JNA 集成。
- `cloud-agent`：宿主机 Agent、虚拟机操作以及与管理端的通信。
- `cloud-management`：管理端 API、Web 界面、数据持久化、认证和编排。

## 报告 Bug

在提交 Bug 报告之前，请先完成以下事项：

1. 在你使用的仓库托管平台搜索已有 Issue。
2. 确认使用最新的开源代码可以复现该问题。
3. 查看 [README.md](README.md) 或 [README-en.md](README-en.md) 中的配置说明和故障排查内容。
4. 如果怀疑是安全漏洞，请不要提交公开 Issue，应按照[安全问题报告](#安全问题报告)中的流程处理。

提交 Issue 时，请使用清晰的标题，例如 `libvirtd 重启后 Agent 无法重新连接`，并提供以下信息。

### 环境信息

- KVM Cloud 的版本、分支或 commit hash。
- 部署拓扑，以及管理端与 Agent 的版本是否一致。
- 宿主机操作系统、架构和内核版本（`cat /etc/os-release`、`uname -a`）。
- QEMU/KVM 版本（CentOS 上可执行 `/usr/libexec/qemu-kvm --version`，其他操作系统使用对应命令）。
- libvirt 版本（`libvirtd --version`，必要时同时提供 `virsh version`）。
- Java 版本（`java -version`）。
- 构建相关问题所需的 Maven 版本（`mvn -version`）。
- 数据库结构或持久化相关问题所需的 MySQL 服务端版本（`SELECT VERSION();`）。
- 相关存储后端（本地、NFS、GlusterFS 或 Ceph RBD）和网络模式（FLAT/VLAN、Linux Bridge/OVS）。
- 虚拟机初始化相关问题所需的 Guest OS、系统模版和 cloud-init 版本。

### 问题详情

- 预期行为与实际行为。
- 能够稳定复现问题的最少操作步骤。
- 问题是持续出现还是偶发。
- 相关的管理端、Agent、libvirt 或虚拟机日志，尽可能包含时间戳。
- 复现问题所需的最小配置片段。
- UI 问题所需的截图，以及相关浏览器版本。
- 最近进行过的配置、数据库结构、系统模版或软件包变更。

请对日志、配置和截图中的密码、令牌、JWT、API Key、私钥、数据库凭据、内部 IP 地址及其他敏感信息进行脱敏。不要上传专有虚拟机镜像或生产数据。

## 提出功能建议

在开始较大规模的开发工作之前，请先提交 Issue，并说明：

- 需要解决的问题或实际运维需求，而不仅是设想的实现方式。
- 目标用户和具体使用场景。
- 期望的功能行为，以及配置、API 和 UI 变更。
- 与现有部署的兼容性及可能的迁移步骤。
- 对安全性、可靠性、性能和升级的影响。
- 考虑过的替代方案，以及该功能的测试和文档方案。

特别欢迎以下方向的功能建议：

- 多云或外部平台集成。
- 新存储后端，以及现有存储池能力改进。
- 新网络模式、OVS/SDN 能力和网络故障排查改进。
- cloud-init 数据源、系统模版和虚拟机初始化改进。

对于大型变更，请在提交 Pull Request 之前与维护者就范围和设计达成一致。保持开源基线可以独立构建，不要提交专有代码或机密代码。

## 开发环境搭建

### 环境要求

- Git 客户端。
- x86_64 架构的 Linux；CentOS 是当前主要文档覆盖的宿主机平台。Ubuntu 相关说明请参阅 [Ubuntu_README.md](Ubuntu_README.md)。
- OpenJDK/Java 8，编译和运行均必须使用 Java 8。
- Maven 3.x，通过 `mvn -version` 确认 Maven 使用的是 Java 8。
- 用于 Agent 和集成测试的 QEMU/KVM 与 libvirt。
- 用于兼容验证的 MySQL 8.0 开发数据库。必须使用隔离的数据库，不得使用生产实例。
- 用于创建或修改虚拟机测试的独立 KVM 宿主机，或启用嵌套虚拟化的开发虚拟机。


按照 [README.md](README.md) 完成 SELinux、sysctl、防火墙、NFS、KVM、网桥、VNC 和 libvirtd 配置。部分示例会开放服务或关闭主机安全防护，仅可在隔离的开发宿主机上使用；请根据实际环境调整，不要直接应用到共享或生产系统。

### 项目构建

```sh
git clone https://github.com/cj520120/kvm-cloud.git
cd kvm-cloud
mvn clean package -Dfile.encoding=UTF-8 -DskipTests=true
```

构建完成后，管理端和 Agent 的可执行 JAR 分别生成在对应模块的 `target` 目录下。不要提交生成的 `target` 内容。

### 配置与运行

请在版本控制之外创建本地配置文件，或者将默认配置复制为不会提交的工作文件：

```sh
cp cloud-management/src/main/resources/application.yaml server.yaml
cp cloud-agent/src/main/resources/application.properties client.properties
```

修改管理端 URI、数据库连接、宿主机网络及其他环境相关配置。开发时应使用全新的隔离数据库。运行 SQL 脚本前务必检查其内容：`scripts/mysql.sql` 是完整的安装/导出脚本，包含具有破坏性的 `DROP TABLE` 语句；`cloud-management/src/main/resources/schema-mysql.sql` 则由管理端应用用于初始化数据库结构。

使用明确的本地配置分别启动两个服务：

```sh
java -jar cloud-management/target/cloud-management-1.0-SNAPSHOT.jar --spring.config.location=server.yaml
java -jar cloud-agent/target/cloud-agent-1.0-SNAPSHOT.jar --spring.config.location=client.properties
```

不得提交本地配置文件、真实数据库凭据、访问令牌、私钥或其他敏感信息。

## 代码风格规范

### 通用规范

- 保持变更聚焦，避免无关的重命名、格式化、依赖升级或生成文件变更。
- 遵循所修改模块的现有约定，并保持 UTF-8 编码。
- 使用清晰的命名，仅在行为或约束不够直观时添加注释。
- 不得屏蔽错误、忽略失败测试或隐瞒配置问题。

### Java

- 以 Java 8 为目标版本，不得使用更高 Java 版本引入的语言特性或标准库功能。
- 使用四个空格缩进；类型使用 `UpperCamelCase`，方法和字段使用 `lowerCamelCase`，常量使用 `UPPER_SNAKE_CASE`。
- 遵循现有 `cn.ruiyun.cloud` 包结构，避免无关的包重命名。
- 避免新增通配符导入、未使用的导入和未使用的依赖。
- 新增 Spring 组件优先使用构造器注入。Controller 应聚焦 HTTP 处理，业务逻辑放在 Service 中。
- 在 API 和配置边界校验外部输入。除非变更有意调整相关设计，否则保持现有 API 响应和异常处理约定。
- 在需要事务的 Service 层使用事务，并尽可能缩小事务边界。
- 显式处理 libvirt、进程、Socket、文件和数据库异常，不得捕获后忽略。
- 日志中不得输出密码、令牌、签名、私钥、完整连接凭据或敏感的虚拟机数据。

### Maven 与依赖

- 保持与 Java 8、Spring Boot 2.3.3 及现有模块结构的兼容性。
- 在实际使用依赖的模块中声明依赖。优先复用父 POM 管理的版本，必要时在父 POM 中定义版本属性。
- 使用范围最小且正确的依赖 scope；测试库必须保持为 `test` scope。
- 不要提交本地 Maven 仓库、IDE 设置、构建产物或依赖缓存。
- Spring Boot、Java、libvirt 绑定及其他基础依赖的升级应作为独立变更，并进行完整的兼容性测试。

### Shell 脚本

- 新的 Bash 脚本以 `#!/usr/bin/env bash` 开头，并通常启用 `set -euo pipefail`。
- 为变量展开和路径添加引号，校验必要输入，并在使用前检查所需命令是否存在。
- 对特权操作，以及网络、防火墙、存储或 libvirt 的修改向用户作出明确提示。
- 避免默认执行破坏性操作，提供安全的失败行为，并清理临时资源。
- 对修改的脚本执行 `bash -n`，并在可用时使用 ShellCheck。按文档声明的目标操作系统测试实际行为。

### SQL 与数据库变更

- 遵循现有 MySQL 表和字段命名约定，新增表和文本字段使用 `utf8mb4` 字符集。
- SQL 文件中不得包含生产数据、凭据、主机地址或个人信息。
- 当数据库结构变更影响全新安装时，保持 `cloud-management/src/main/resources/schema-mysql.sql` 与 `scripts/mysql.sql` 一致，同时注意两者用途不同。
- 需要时为现有部署提供升级 SQL 和清晰的升级说明，不得将删表重建作为升级方案。
- 根据结构变更补充合适的键、索引、约束、默认值和空值约束。
- 在 MySQL 8.0 上分别使用全新数据库和旧结构数据库副本进行验证，并在适用时确认应用初始化可以安全地重复执行。

### Spring Boot 与配置

- 环境相关值必须使用外置配置，不得硬编码本地主机、凭据、路径或令牌。
- 除非变更明确包含配置迁移，否则沿用管理端 `application.yaml` 和 Agent `application.properties` 的现有约定。
- 记录新增配置项、默认值、有效值及升级影响。
- 保留管理端接口的认证和权限校验，将管理端与 Agent 之间的消息及凭据视为不可信输入。
- 尽可能保持与现有 API 客户端的兼容性，并记录所有有意引入的破坏性变更。

## 测试要求

仓库目前的自动化测试覆盖有限。新贡献应改善测试覆盖情况，而不是只依赖手工验证。

### 单元测试

- 为新逻辑和 Bug 修复在受影响模块的 `src/test/java` 目录下添加针对性测试。
- 使用 JUnit 4 和项目现有测试约定，可以使用 Mockito 模拟外部协作者。
- 单元测试不得依赖 KVM、libvirtd、MySQL、网络桥接或正在运行的管理端/Agent 服务。
- 覆盖与变更相关的正常行为、边界条件和失败路径。
- 提交前运行完整的 Maven Reactor 验证：

```sh
mvn clean verify -Dfile.encoding=UTF-8
```

例如，测试单个模块及其所需依赖时，可以执行：

```sh
mvn -pl cloud-management -am test -Dfile.encoding=UTF-8
```

### KVM 与集成测试

- 在具有可用 KVM 和 libvirt 的独立宿主机上测试 libvirt 和虚拟机生命周期变更。
- 依赖真实基础设施的集成测试必须显式启用，不得在默认单元测试构建中自动运行。
- 验证受影响的操作及相关失败路径，例如 Agent 重连、虚拟机启停、磁盘挂载、存储访问、网络配置或 cloud-init 初始化。
- 不得对生产虚拟机、存储池、网络或数据库执行破坏性测试。
- 清理测试创建的虚拟机、磁盘、网络、挂载点、临时文件及其他资源。
- 在 Pull Request 中记录准确的测试命令，以及宿主机 OS、QEMU/KVM、libvirt、Java、存储和网络信息。

### SQL 与脚本验证

- 分别在空数据库和旧结构数据库副本上验证修改过的 SQL。
- 验证全新安装、升级行为、应用启动及相关 CRUD 操作。
- 不要在数据库的唯一副本上验证脚本，必须先创建可恢复的备份。
- 对 Shell 脚本执行语法检查，并在隔离环境中手工验证涉及特权操作的变更。

如果变更仅涉及文档，请在 Pull Request 中注明 `Tests not run (documentation only)`。其他变更应列出实际执行的命令及结果；不得声称执行过实际并未运行的测试。

## 文档更新要求

当功能行为、安装方式、配置、API 或故障排查方法发生变化时，请在同一个 Pull Request 中更新文档：

- 保持 [README.md](README.md) 与 [README-en.md](README-en.md) 同步。
- Ubuntu 特有的安装或行为差异应更新 [Ubuntu_README.md](Ubuntu_README.md)。
- OVS 网桥或网络配置变更应更新 [ovs-bridge-sample.md](scripts/ovs-bridge-sample.md)。
- 对新发现的故障模式及经过验证的解决方案，新增或修订故障排查条目。
- 更新受影响的配置示例、SQL 说明、API 文档和截图。
- 使用对应语言的截图，并移除主机名、IP 地址、令牌及其他隐私信息。

不得将未经验证的临时方案记录为已确认的解决方案。

## Pull Request 流程

1. Fork 仓库，并基于当前默认分支创建一个聚焦单一目标的分支。
2. 关联相关 Issue；重大设计变更应在实现前进行讨论。
3. 保持提交精简且易于理解，使用能够说明变更目的的简洁 commit message。
4. 执行相关的构建、单元测试、集成测试、SQL 和脚本检查。
5. 在适用时同步更新中英文文档。
6. 检查最终 diff，移除调试代码、生成文件、本地配置和无关变更。
7. 提交 Pull Request，并包含：
   - 问题背景及所选方案的摘要。
   - 受影响的模块和用户可见行为。
   - 配置、数据库、API、兼容性和升级影响。
   - 已执行的测试、准确命令、结果及相关环境版本。
   - UI 变更截图和更新后的文档链接。
   - 仍存在的限制或后续工作。
8. 响应评审意见并保持分支更新，直至完成所有要求的检查和评审。

提交前请确认：

- 项目能够使用 Java 8 构建。
- 新增及现有的相关测试均通过。
- 数据库结构变更同时涵盖全新安装和升级处理。
- 文档已同步且内容准确。
- 不包含敏感信息、生成的构建产物或生产数据。

## 安全问题报告

**请通过邮件 [cj520120@163.com](mailto:cj520120@163.com) 提交安全漏洞，严禁通过公开 Issue、Pull Request、评论或截图披露。**

邮件主题请统一使用 `SECURITY: kvm-cloud`。如果需要安全渠道传递敏感的复现详情，请先通过该邮箱联系，再发送相关内容。

报告中请包含：

- 受影响的版本或 commit，以及相关模块/组件。
- 漏洞描述及其潜在影响。
- 仅在你自己的隔离环境中验证的复现步骤或概念证明。
- 相关且已脱敏的日志或配置，以及建议的缓解措施。
- 便于协调披露流程的联系方式。

以下方向的安全报告尤其重要：

- Agent 与管理端之间的通信、注册、消息签名和信任边界。
- 身份认证、JWT 签发与校验、令牌刷新、会话和权限执行。
- 数据库凭据、API Key/AppKey、私钥及其他敏感信息。
- VNC、libvirt、WebSocket 和管理端 API 的暴露风险。
- 命令执行、虚拟机初始化，以及宿主机文件或网络访问。

不得提供真实凭据、生产数据，或针对非本人所有系统的可用攻击方案。在公开披露之前，请给予维护者合理的时间进行调查和修复。如果不确定问题是否涉及安全，请先通过非公开渠道报告。
