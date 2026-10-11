# 梁山集团企业领域模型 Demo

本地主数据演示，覆盖法人、公司（含分公司）、自然人、员工和部门。公司下面可以维护登记版本、银行账户、股东明细和公司版本；部门可以维护服务公司和部门版本。

模型依据 `docs/` 里的领域说明和逻辑 Schema。数据库只保存主键、基本类型、`NOT NULL` 和普通索引。关联是普通的可空 `Long`，没有外键、`CHECK` 或业务唯一约束。公司类型、一对一、股东单选和期间重叠由应用层校验。

这一版对应工作包 WP-01 和 WP-02。雇佣、任职、汇报、供应商、项目、合同和财务对象还没有页面。

## 环境

- Java 21
- Maven 3.8+
- Spring Boot 4.1
- 本机 SQLite 文件 `./data/liangshan.db`

## 启动

在仓库根目录执行：

```bash
mvn spring-boot:run
```

浏览器打开 [http://127.0.0.1:18480/](http://127.0.0.1:18480/)。

导航可以进入：

| 页面 | 地址 |
|---|---|
| 首页 | http://127.0.0.1:18480/ |
| 法人 | http://127.0.0.1:18480/legal-persons |
| 公司 | http://127.0.0.1:18480/companies |
| 自然人 | http://127.0.0.1:18480/persons |
| 员工 | http://127.0.0.1:18480/employees |
| 部门 | http://127.0.0.1:18480/departments |

列表页进入新建或编辑。公司、法人和部门的编辑页里可以直接增删所属明细。

也可以先打包再运行：

```bash
mvn -DskipTests package
java -jar target/liangshan-demo-0.1.0.jar
```

## 数据库

`src/main/resources/application.yml` 使用 `spring.jpa.hibernate.ddl-auto=update`。重启后 `./data/liangshan.db` 会保留。SQLite 对改表的支持有限；实体字段有破坏性变化时，先停掉应用，删除 `./data/liangshan.db`，再启动，让 Hibernate 按当前实体重建。

测试使用另一份库 `target/test-liangshan.db`，并用 `ddl-auto=create`。

```bash
mvn test
```

## 应用层约定

- 未知的号码、金额和可选关联保存为 `NULL`，不写空字符串。
- `company_type=company` 必须有 `legal_person_id`，`head_company_id` 留空，并填写公司形式。同一法人只能对应一家公司。
- `company_type=branch` 必须有指向公司类型记录的 `head_company_id`，不能指向自己。法人、公司形式、注册资本和股东留空。
- 股东在公司、法人、自然人中恰好选一个。公司股东必须是公司类型。持股比例按 0 到 1 保存。
- 当前未结束的 `company_registration` 会在同一事务写回公司上的证照、税号、注册资本和营业期限。跨日修改登记或公司核心资料时保留旧版本。
- 员工通过 `person_id` 与自然人一对一。部门编号在所属公司内不重复，行政上级不能成环。
