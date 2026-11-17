English | [中文](CONTRIBUTING.md)

# Contributing to KVM Cloud

Thank you for your interest in contributing to KVM Cloud. This guide explains how to report problems, propose improvements, set up a development environment, and submit changes to the open-source repository.

KVM Cloud is a Java 8 and Spring Boot project with three Maven modules:

- `cloud-common`: shared models, utilities, and libvirt/JNA integration.
- `cloud-agent`: host-side agent, VM operations, and management communication.
- `cloud-management`: management APIs, web interface, persistence, authentication, and orchestration.

## Open-source and Subscription Editions

This guide applies only to the open-source repository. The subscription edition includes independently licensed enterprise enhancements. Its source code, licensing, delivery, and support are not provided through this repository or pull requests. Open-source contributions must not depend on proprietary subscription-edition components and must keep the open-source edition independently buildable and runnable. For subscription-related requests, use the official [contact page](https://www.ruiyuncloud.cn/#contact).

Security vulnerabilities should still be reported privately by email as described in [Security Reporting](#security-reporting).

## Reporting Bugs

Before opening a bug report:

1. Search existing issues on the repository host you use.
2. Confirm that the problem can be reproduced with the latest open-source code.
3. Check the configuration and troubleshooting sections in [README.md](README.md) or [README-en.md](README-en.md).
4. Do not use a public issue for a suspected security vulnerability; follow [Security Reporting](#security-reporting) instead.

Open an issue with a clear title, such as `Agent fails to reconnect after libvirtd restart`, and include the following information.

### Environment

- KVM Cloud release, branch, or commit hash.
- Deployment topology and whether the management and agent versions match.
- Host OS, architecture, and kernel version (`cat /etc/os-release`, `uname -a`).
- QEMU/KVM version (`/usr/libexec/qemu-kvm --version` on CentOS, or the equivalent command on your OS).
- libvirt version (`libvirtd --version` and, when useful, `virsh version`).
- Java version (`java -version`).
- Maven version for build problems (`mvn -version`).
- MySQL server version for schema or persistence problems (`SELECT VERSION();`).
- Storage backend (local, NFS, GlusterFS, or Ceph RBD) and network mode (FLAT/VLAN, Linux Bridge/OVS), when relevant.
- Guest OS, template, and cloud-init version for guest initialization problems.

### Problem Details

- What you expected to happen and what actually happened.
- The smallest reliable set of reproduction steps.
- Whether the problem is consistent or intermittent.
- Relevant management, agent, libvirt, or guest logs, with timestamps where possible.
- Minimal configuration excerpts needed to reproduce the problem.
- Screenshots for UI problems and the browser version when relevant.
- Any recent configuration, schema, template, or package changes.

Redact passwords, tokens, JWTs, API keys, private keys, database credentials, internal IP addresses, and other sensitive data from logs, configuration, and screenshots. Do not attach proprietary VM images or production data.

## Suggesting Features

Open an issue before starting substantial work. Describe:

- The problem or operational need, not only the proposed implementation.
- The intended users and a concrete usage scenario.
- Proposed behavior, configuration, API, and UI changes.
- Compatibility with existing installations and possible migration steps.
- Security, reliability, performance, and upgrade implications.
- Alternatives considered and how the feature could be tested and documented.

Feature proposals are especially welcome in areas such as:

- Multi-cloud or external platform integration.
- New storage backends and improvements to existing storage pools.
- New network modes, OVS/SDN capabilities, and network troubleshooting.
- cloud-init datasource, template, and guest initialization improvements.

For large changes, agree on the scope and design with maintainers before opening a pull request. Keep the open-source baseline independently buildable and do not include proprietary or confidential code.

## Development Setup

### Prerequisites

- A Git client.
- Linux on x86_64; CentOS 7 is the primary documented host platform. For Ubuntu-specific instructions, see [Ubuntu_README.md](Ubuntu_README.md).
- OpenJDK/Java 8. Both compilation and runtime must use Java 8.
- Maven 3.x. Confirm with `mvn -version` that Maven is using Java 8.
- QEMU/KVM and libvirt for agent and integration testing.
- MySQL 8.0 for a compatible development database. Use an isolated database, never a production instance.
- A dedicated KVM host, or a development VM with nested virtualization, for tests that create or modify VMs.

Redis is not a project dependency and does not need to be installed.

Follow [README.md](README.md) for SELinux, sysctl, firewall, NFS, KVM, bridge, VNC, and libvirtd configuration. Several examples deliberately open services or disable host protections; use them only on an isolated development host and adjust them to your environment rather than applying them to shared or production systems.

### Build

```sh
git clone https://github.com/cj520120/kvm-cloud.git
cd kvm-cloud
mvn clean package -Dfile.encoding=UTF-8 -DskipTests=true
```

The build produces the management and agent JARs under their respective `target` directories. Do not commit generated `target` content.

### Configure and Run

Create local configuration files outside version control, or copy the defaults to working files that you will not commit:

```sh
cp cloud-management/src/main/resources/application.yaml server.yaml
cp cloud-agent/src/main/resources/application.properties client.properties
```

Edit the management URI, database connection, host networking, and other environment-specific values. Use a new isolated database for development. Review SQL scripts before running them: `scripts/mysql.sql` is a full installation/dump script and contains destructive `DROP TABLE` statements, while `cloud-management/src/main/resources/schema-mysql.sql` is used by the management application for schema initialization.

Start each service with its explicit local configuration:

```sh
java -jar cloud-management/target/cloud-management-1.0-SNAPSHOT.jar --spring.config.location=server.yaml
java -jar cloud-agent/target/cloud-agent-1.0-SNAPSHOT.jar --spring.config.location=client.properties
```

Never commit local configuration files, real database credentials, access tokens, private keys, or other secrets.

## Code Style Guidelines

### General

- Keep changes focused. Avoid unrelated renames, formatting, dependency upgrades, or generated-file changes.
- Follow the conventions of the module being changed and preserve UTF-8 encoding.
- Use clear names and add comments only where behavior or constraints are not self-evident.
- Do not silence errors, ignore failing tests, or hide configuration problems.

### Java

- Target Java 8; do not use language or standard-library features introduced in later Java versions.
- Use four spaces for indentation, `UpperCamelCase` for types, `lowerCamelCase` for methods and fields, and `UPPER_SNAKE_CASE` for constants.
- Follow the existing `cn.ruiyun.cloud` package structure. Avoid unrelated package renaming.
- Avoid new wildcard imports, unused imports, and unused dependencies.
- Prefer constructor injection for new Spring components. Keep controllers focused on HTTP concerns and place business logic in services.
- Validate external input at API and configuration boundaries. Preserve existing API response and error-handling conventions unless a change intentionally revises them.
- Use transactions at the service layer where required, and keep transaction boundaries as small as practical.
- Handle libvirt, process, socket, file, and database failures explicitly. Do not catch and ignore exceptions.
- Never log passwords, tokens, signatures, private keys, full connection credentials, or sensitive guest data.

### Maven and Dependencies

- Preserve compatibility with Java 8, Spring Boot 2.3.3, and the existing module structure.
- Declare dependencies in the module that uses them. Reuse versions managed by the parent POM or define a parent property when appropriate.
- Use the narrowest correct dependency scope; test libraries must remain in `test` scope.
- Do not commit local Maven repositories, IDE settings, build output, or dependency caches.
- Upgrade Spring Boot, Java, libvirt bindings, or other foundational dependencies in a dedicated change with full compatibility testing.

### Shell Scripts

- Start new Bash scripts with `#!/usr/bin/env bash` and normally use `set -euo pipefail`.
- Quote variable expansions and paths, validate required input, and check required commands before use.
- Make privileged operations and changes to networking, firewalls, storage, or libvirt explicit to the user.
- Avoid destructive defaults. Provide safe failure behavior and clean up temporary resources.
- Run `bash -n` on changed scripts and ShellCheck when available. Test behavior on the documented target OS.

### SQL and Database Changes

- Follow the existing MySQL table and column naming conventions, and use `utf8mb4` for new textual schema objects.
- Never include production data, credentials, host addresses, or personal information in SQL files.
- Keep `cloud-management/src/main/resources/schema-mysql.sql` and `scripts/mysql.sql` consistent when a schema change affects fresh installations, while respecting their different purposes.
- Provide upgrade SQL and clear upgrade instructions for existing installations when required. Do not use dropping and recreating tables as an upgrade strategy.
- Add appropriate keys, indexes, constraints, defaults, and nullability as part of the schema change.
- Validate against MySQL 8.0 on both a fresh database and a copy of the previous schema. Confirm that application initialization can be rerun safely where applicable.

### Spring Boot and Configuration

- Use externalized configuration for environment-specific values; do not hard-code local hosts, credentials, paths, or tokens.
- Preserve the existing management `application.yaml` and agent `application.properties` conventions unless a migration is explicitly part of the change.
- Document new configuration keys, defaults, valid values, and upgrade impact.
- Preserve authentication and permission checks on management endpoints. Treat agent-management messages and credentials as untrusted input.
- Keep changes compatible with existing API clients where possible and document any intentional breaking change.

## Testing Requirements

The repository currently has limited automated test coverage. New contributions should improve coverage rather than relying only on manual verification.

### Unit Tests

- Add focused tests under the affected module's `src/test/java` directory for new logic and bug fixes.
- Use JUnit 4 and the project's existing test conventions. Mockito may be used for external collaborators.
- Unit tests must not require KVM, libvirtd, MySQL, a network bridge, or a running management/agent service.
- Cover normal behavior, boundary conditions, and failure paths relevant to the change.
- Run the full reactor verification before submitting:

```sh
mvn clean verify -Dfile.encoding=UTF-8
```

For a single module and its required dependencies, use for example:

```sh
mvn -pl cloud-management -am test -Dfile.encoding=UTF-8
```

### KVM and Integration Tests

- Test libvirt and VM lifecycle changes on a dedicated host with working KVM and libvirt.
- Integration tests that require real infrastructure must be explicitly opt-in and must not run during the default unit-test build.
- Exercise the affected operation and adjacent failure paths, such as agent reconnection, VM start/stop, disk attachment, storage access, network configuration, or cloud-init initialization.
- Do not run destructive tests against production VMs, storage pools, networks, or databases.
- Clean up created VMs, disks, networks, mount points, temporary files, and other resources.
- Record the exact test command, host OS, QEMU/KVM, libvirt, Java, storage, and network details in the pull request.

### SQL and Script Validation

- Validate changed SQL on an empty database and on a copy of the previous schema.
- Verify fresh installation, upgrade behavior, application startup, and relevant CRUD operations.
- Do not validate scripts on the only copy of a database; take a restorable backup first.
- Run syntax checks on shell scripts and manually test changed privileged operations in an isolated environment.

If a change is documentation-only, state `Tests not run (documentation only)` in the pull request. Otherwise, list the commands actually run and their results; never claim tests were run when they were not.

## Documentation Updates

Update documentation in the same pull request whenever behavior, setup, configuration, APIs, or troubleshooting changes:

- Keep [README.md](README.md) and [README-en.md](README-en.md) synchronized.
- Update [Ubuntu_README.md](Ubuntu_README.md) for Ubuntu-specific installation or behavior differences.
- Update [ovs-bridge-sample.md](scripts/ovs-bridge-sample.md) for OVS bridge or network configuration changes.
- Add or revise troubleshooting entries for newly discovered failure modes and their verified solutions.
- Update configuration examples, SQL instructions, API documentation, and screenshots when affected.
- Use language-appropriate screenshots and remove hostnames, IP addresses, tokens, and other private information.

Do not document unverified workarounds as confirmed solutions.

## Pull Request Process

1. Fork the repository and create a focused branch from the current default branch.
2. Link the related issue and discuss substantial design changes before implementation.
3. Keep commits small and understandable. Use concise commit messages that explain the purpose of the change.
4. Run the relevant build, unit, integration, SQL, and script checks.
5. Update both Chinese and English documentation where applicable.
6. Review the final diff and remove debugging code, generated files, local configuration, and unrelated changes.
7. Open a pull request with:
   - A summary of the problem and the chosen solution.
   - The affected modules and user-visible behavior.
   - Configuration, database, API, compatibility, and upgrade impacts.
   - Tests performed, exact commands, results, and relevant environment versions.
   - Screenshots for UI changes and links to updated documentation.
   - Any remaining limitations or follow-up work.
8. Respond to review feedback and keep the branch up to date until all requested checks and reviews are complete.

Before submitting, confirm that:

- The project builds with Java 8.
- New and existing relevant tests pass.
- Schema changes include fresh-install and upgrade handling.
- Documentation is synchronized and accurate.
- No secrets, generated build output, or production data are included.

## Security Reporting

**Report security vulnerabilities by emailing [cj520120@163.com](mailto:cj520120@163.com). Do not disclose them through public issues, pull requests, comments, or screenshots.**

Use `SECURITY: kvm-cloud` as the email subject. If a secure channel is needed for sensitive reproduction details, request one through this email address before sending them.

Include:

- The affected release or commit and the impacted modules/components.
- A description of the vulnerability and its potential impact.
- Reproduction steps or a proof of concept using only your own isolated environment.
- Relevant sanitized logs or configuration and any suggested mitigation.
- Your preferred contact information for coordinated disclosure.

Security reports are particularly important for:

- Agent-management communication, registration, message signing, and trust boundaries.
- Authentication, JWT issuance/validation, token refresh, sessions, and permission enforcement.
- Database credentials, API keys/AppKeys, private keys, and other secrets.
- VNC, libvirt, WebSocket, and management API exposure.
- Command execution, guest initialization, and host file or network access.

Never include real credentials, production data, or a working exploit against systems you do not own. Give maintainers a reasonable opportunity to investigate and remediate the issue before public disclosure. If you are unsure whether a problem is security-sensitive, report it privately first.
