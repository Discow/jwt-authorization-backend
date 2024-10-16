# JWT登录验证演示-后端
## 介绍
使用 JWT（JSON Web Token）进行身份验证时，通常涉及两个重要的令牌：**Access Token** 和 **Refresh Token**。
- 访问令牌（Access Token）
  - **Access Token** 是一种短期有效的令牌，主要用于身份验证和授权。它通常包含用户的身份信息和权限，在每次请求受保护的资源时都需要附带这个令牌。
- 刷新令牌（Refresh Token）
  - **Refresh Token** 是一种长期有效的令牌，主要用于获取新的 Access Token。当 Access Token 过期时，使用 Refresh Token 可以请求新的 Access Token，而无需重新登录。

JWT与Session认证的对比：

| 特性         | JWT认证                               | Session认证                      |
|------------|-------------------------------------|--------------------------------|
| **存储位置**   | 客户端存储（通常在 Local Storage 或 Cookie 中） | 服务器端存储（在内存、数据库或缓存中）            |
| **状态**     | 无状态（Stateless），服务器不保存用户状态           | 有状态（Stateful），服务器保存用户会话状态      |
| **扩展性**    | 易于扩展，适合分布式系统（微服务架构）                 | 不易扩展，服务器需要管理会话信息               |
| **性能**     | 通过减少服务器存储负担提高性能                     | 需要频繁访问服务器存储的会话数据，可能影响性能        |
| **安全性**    | 对于用户信息和签名验证的安全性较高，但需注意令牌泄露          | 依赖于服务器的安全性，通常会话存储在服务器端         |
| **失效管理**   | 需要在令牌过期后，客户端需要使用 Refresh Token 更新   | 可以随时使会话失效，便于管理                 |
| **跨域支持**   | 支持跨域，易于与不同服务进行认证                    | 跨域支持有限，需配置跨域资源共享（CORS）         |
| **易用性**    | 可以通过简单的 HTTP 请求在多个系统中使用             | 需要在每个请求中附加 session ID 或 cookie |
| **移动应用支持** | 适合移动应用，令牌可以存储在客户端，并轻松使用             | 移动应用需保持会话，可能会有额外复杂性            |
| **签名验证**   | JWT可以使用对称或非对称加密进行签名                 | Session ID 本身无签名，需要结合其他机制进行验证  |

## 登录流程

- 用户登录
  1. 用户提供凭据：用户通过登录界面提供用户名和密码。
  2. 服务端验证：服务器接收到用户的凭据后，验证其合法性（如查询数据库确认用户名和密码）。
  3. 生成令牌：
      - 生成 Access Token：在验证成功后，服务器生成 Access Token，通常包含用户信息、过期时间和签名。
      - 生成 Refresh Token：同时，服务器也生成一个 Refresh Token，这个令牌通常有效期较长（如几天到几周）。
  4. 返回令牌：服务器将 Access Token 和 Refresh Token 一并返回给客户端。

- 访问资源
  1. 客户端请求：客户端在请求需要身份验证的受保护资源时，将 Access Token 附加到请求头中，通常使用 `Authorization: Bearer <access_token>` 格式。
  2. 服务器验证：
      - 服务器解析 Access Token，验证其合法性（如签名和过期时间）。
      - 如果 Access Token 合法且未过期，服务器处理请求并返回相应的数据。
      - 如果 Access Token 已过期，则返回 401 Unauthorized 错误。

- 刷新 Access Token：当 Access Token 过期后，客户端可以使用 Refresh Token 来获取新的 Access Token，而无需重新登录。
  1. 客户端请求刷新：
      - 客户端检测到 Access Token 已过期，向服务器发起续签请求，并携带 Refresh Token。
  2. 服务器验证 Refresh Token：
      - 服务器解析 Refresh Token，验证其有效性（如签名、过期时间以及是否在黑名单中）。
      - 如果 Refresh Token 有效，服务器生成新的 Access Token，并生成新的 Refresh Token（以延长登录会话）。
  3. 返回新令牌：服务器将新的 Access Token（以及可能的新 Refresh Token）返回给客户端。

- 过期与撤销策略
  - 一般情况下，Access Token 的有效期较短（如 15 分钟到 1 小时），以降低安全风险。
  - Refresh Token 的有效期较长，通常为几天到几周。为了提高安全性，服务器可以实现 Refresh Token 的黑名单机制，手动撤销某些 Refresh Token（例如，用户主动登出时）。
  - 在 Refresh Token 过期后，用户必须重新登录，以获取新的 Access Token 和 Refresh Token。


## 快速开始
**先决条件**：Java17、MySQL8.0、Redis
1. 配置数据库信息、JWT secret key、redis主机地址。（可以直接修改application.yml，或配置环境变量。）
   - 数据库表结构：见下表。
   - JWT secret key：建议随机生成，并确保不被泄露。
   - access-token-expire：短令牌过期时间，超过该时间需要进行续签操作。
   - refresh-token-expire：长令牌过期时间，超过该时间需要用户重新登录。
2. 使用`mvn spring-boot:run`启动项目
3. 使用API测试工具进行测试：
   - [登录](http://localhost:8080/api/auth/login)
      - 请求类型：POST
      - 内容类型：application/x-www-form-urlencoded
      - 参数：username、password
   - [令牌续签](http://localhost:8080/api/auth/refresh-token)
      - 请求类型：POST
      - 内容类型：application/x-www-form-urlencoded
      - 参数：refreshToken、accessToken
   - 测试API：
     - 请求类型：GET
     - 需要携带请求头：Authorization: ${accessToken}
     - API：
        - [public](http://localhost:8080/test/public)：已验证用户可访问
        - [user](http://localhost:8080/test/user)：已验证且角色为USER的用户可访问
        - [admin](http://localhost:8080/test/admin)：已验证且角色为ADMIN的用户可访问

**数据库示例**：

| uid (unsigned int) | email (varchar) | username (varchar) |  password (varchar)   | role (enum('USER', 'ADMIN')) |
|:------------------:|:---------------:|:------------------:|:---------------------:|:----------------------------:|
|       10001        | admin@admin.com |       admin        | YOUR_PASSWORD(BCrypt) |            ADMIN             |
|       10002        |  test@test.com  |        test        | YOUR_PASSWORD(BCrypt) |             USER             |
