# JWT登录验证演示-后端

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
