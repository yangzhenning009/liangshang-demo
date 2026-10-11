# 企业领域逻辑 Schema 第一版

本草案覆盖法人、公司、分公司、员工、部门、供应商管理，以及项目合同、分阶段履约、工时、费用、发票、收付款和账务。不限定数据库产品；物理设计遵循下述数据库约定。

## 本次设计决定

删除统一的 `party` 身份表、各种主体 profile 和通用参与方／关系表。法人、公司、自然人、员工、部门各自拥有对象、主键和专属属性。公司和分公司统一使用 `company`，由 `company_type` 区分，每条记录都有自己的 ID。业务对象直接保存具体对象的普通 ID 字段，角色由字段名表达，例如 `buyer_company_id`、`issuer_company_id`、`payer_company_id`。

法人对象与公司对象分别维护法律信息和公司信息。`company_type = company` 的记录填写 `legal_person_id`，应用层维护其与法人对象的一对一关系；`company_type = branch` 的记录将 `legal_person_id` 留空，通过 `head_company_id` 记录总公司。子公司仍为 `company` 类型，其股权关系由股东明细表达。

一对一或单值关联直接放在对象字段里；重复项放在该对象拥有的类型化明细中，例如 `employee.employments[]`、`project.delivery_companies[]`、`payment.allocations[]`。关系数据库可以把这些明细落为带所属对象关联 ID 的子表。明细用于保存多项数据、期间和分配金额，不设统一身份或通用关系中心。

这种设计让字段含义及关联目标明确。新增参与对象类型时，需要显式增加字段及应用层处理逻辑；第一版使用对象类型明确的 ID 字段。

## 关键业务字段必须明确建模

此原则适用于当前 schema 和后续新增对象。凡是用于业务识别、查询、关联、计算、审批或核算的信息，都必须有固定业务含义的字段；重复数据使用字段明确的所属明细表。

- 证照和税号分别使用 `unified_social_credit_code`、`business_license_registration_number`、`taxpayer_identification_number`；不用“标识类型＋标识值”承载这些关键字段。
- 关联字段明确对象类型和业务角色，如 `buyer_company_id`、`payer_company_id`；不用“对象类型＋对象 ID”或通用参与方关系代替。
- 金额、日期和状态按其所属业务定义。合同价款、费用金额、付款金额分别保存；验收结果、开票状态、付款状态分别表达，不通过一个通用值字段切换含义。
- 不用属性名／属性值明细、JSON 扩展包、`metadata`、`extra` 或备注承载关键业务事实。备注及描述只补充说明，不能替代已有专用字段。
- 新增关键业务概念时，补充专用字段或专属明细，并明确含义、类型、可空性、来源及应用层校验；暂未设计的字段明确列为待定，不能用通用属性占位后视为完成。

`company_type`、`payment_channel` 等有明确含义的分类字段可以保留；其取值由应用层处理。`id`、`created_at` 等基础字段可以按统一规范命名。具体对象内的名称、状态、金额等字段，必须在该对象范围内保持固定含义。

## 数据库约定

- 数据库只使用基本字段类型、`PRIMARY KEY`、`NOT NULL` 和简单常量默认值；查询需要时可建普通索引。
- 不创建外键（`FOREIGN KEY`／`REFERENCES`）、触发器、`CHECK`、排他约束、自定义约束函数或其他承载业务规则的数据库约束；不以存储过程实现业务校验。
- 除主键自身的唯一性外，不增加业务 `UNIQUE` 约束或唯一索引。文中的“一对一”“唯一”“不重复”都是应用层业务规则。
- 所有 `*_id` 关联字段都是普通字段。文中的 `→` 仅说明这个 ID 对应哪个对象，不能据此生成数据库外键、级联更新或级联删除。
- 单个无条件必填字段使用 `NOT NULL`。可选字段、角色 ID 字段组内的各字段、条件必填字段允许 `NULL`；组内单选和条件必填由应用层判断。
- 引用存在性、角色选择、有效期间、层级无环、状态流转、金额分配上限和借贷平衡等由应用层处理。数据库不自动执行这些业务规则；导入、修改和删除也经过相同业务入口。

本文的业务规则描述预期行为，不表示已实现应用代码。涉及并发写入的去重和金额分配，需要在应用实现时结合事务及并发控制处理，不能只靠写入前的一次查询。

## 建模约定

- 实体使用稳定主键 `id`；名称、证照号和业务编号不代替主键。
- 有历史意义的记录保留 `valid_from`、`valid_to`、`recorded_at`；有效期间按左闭右开处理，空结束时间表示尚未结束。需要更正时增加版本、替代或冲销记录。
- 通用审计字段为 `created_at`、`updated_at`、`created_by`。业务事实不通过删除来抹去历史。
- 金额使用定点数和币种，分别命名合同价款、开票金额、付款金额及成本金额。
- 下文“拥有的明细”均指所属对象负责维护的集合；除显式说明外，不复制被关联对象的主数据。
- 数据类型按含义选用：ID、编号、电话号码和账号为字符串；日期／时点为日期／时间类型；金额、数量、比例为定点数；开关为布尔值。比例统一按 0～1 保存；未知金额与零金额分别表达。
- 除已标明可空或条件必填的字段外，下面新增的事实字段在确认该业务记录时必填；草稿未齐资料可留空，由应用层按状态检查。只有始终必填的基础字段才设置数据库 `NOT NULL`。
- 业务生效／发生日期、单据日期、审批日期和记账日期分别命名。每个业务对象的 `status` 只表示自身生命周期，不能同时代表审批、开票、结算和记账状态。
- 单币种核算为第一版口径：合同金额、阶段价款、相关开票／结算计划按合同币种；一条分配的来源和目标须同币种。跨币种业务在明确汇率政策后扩展，不能默认为 1:1。
- 作废或更正已有引用的事实须保留原记录及替代／撤销关联；已过账凭证通过冲销或调整更正。汇总的已开票、已结算、余额和成本合计从有效明细计算，不另设可独立编辑的总额。

### 直接引用多个对象类型的规则

某个角色允许多种具体对象时，在该业务表内设置一组可空的普通 ID 字段。以付款方为例：

| 字段 | 关联目标 |
|---|---|
| `payer_company_id` | `company.id`，可对应公司或分公司 |
| `payer_legal_person_id` | `legal_person.id` |
| `payer_person_id` | `person.id` |

必填角色由应用层校验恰好填写一个 ID；可选角色由应用层校验最多填写一个。以下列出的每个角色组均适用此规则。组内每个字段在数据库中都允许 `NULL`，不创建组内单选的数据库约束，也不额外建立身份表。

业务角色中的公司和分公司都使用 `*_company_id`，记录实际参与方自己的 ID；`*_legal_person_id` 用于公司范围以外的法人。除总公司目标、股东等另有类型要求的字段外，公司 ID 字段均可记录公司或分公司。不能把分公司的业务关联自动替换为总公司的 ID。员工代表公司签字、提交交付物或审批时，使用独立的 `*_employee_id` 经办字段，不替换公司的交易身份。付款给自然人可使用 `payee_person_id`，员工报销还可记录 `related_employee_id`。

当前交易对手范围为公司、分公司、其他法人和自然人；部门不能作为独立合同、发票或资金收付方。合伙企业等其他非法人组织需要时再建对应对象，不塞入法人表。

## 基础对象

### `legal_person`：法人

保存法人法律信息，法人与法定代表人分别表达。

- `id` 主键
- `legal_name`
- `legal_person_type`：企业法人、事业单位法人、社会团体法人等
- `established_at` / `terminated_at` 可空
- `status`

拥有 `registrations[]` 和 `representative_terms[]`。其中法人登记集合仅保存非公司法人的登记资料；公司和分公司的登记资料统一归属各自的 `company.registrations[]`，同一登记事实只维护一份：

- `legal_person_registration`：`id`、`legal_person_id` → `legal_person.id`、`registration_type`、`unified_social_credit_code`（统一社会信用代码，可空）、`registration_certificate_number`（登记证书号码，可空）、`taxpayer_identification_number`（纳税人识别号，可空）、`registration_authority`、`jurisdiction`、`registered_name`、`registered_address`、`registration_status`、`registered_at`、有效期间、`source_document_id` → `source_document.id`（可空）。号码均使用字符串并按实际资料填写；非公司法人的登记证书号码不命名为营业执照号。
- `legal_representative_term`：`id`、`legal_person_id` → `legal_person.id`、`person_id` → `person.id`、有效期间、`basis_document_id` → `source_document.id`（可空）。

同一法人同一期间的有效法定代表人按适用规则校验。法定代表人不要求是系统中的员工。

### `company`：公司与分公司

公司与分公司共用一个对象，每家公司、分公司分别建立一条记录。

| 字段 | 数据库要求 | 含义 |
|---|---|---|
| `id` | 主键 | 当前公司或分公司自己的 ID |
| `company_code` | `NOT NULL` | 内部编号，业务范围内去重由应用层处理 |
| `name` | `NOT NULL` | 业务名称；登记名称以自身登记记录为准 |
| `company_type` | `NOT NULL`，普通字符串 | `company` 公司、`branch` 分公司；取值由应用层校验 |
| `unified_social_credit_code` | 可空，普通字符串 | 统一社会信用代码，中国现行营业执照上的主要登记标识 |
| `business_license_registration_number` | 可空，普通字符串 | 营业执照注册号／工商注册号，主要保存历史注册号；仅有统一社会信用代码时此字段可空 |
| `taxpayer_identification_number` | 可空，普通字符串 | 纳税人识别号，即税号，记录该公司或分公司实际使用的税务标识 |
| `registered_name` / `registered_address` | 可空，普通字符串 | 登记名称及注册地址 |
| `registration_jurisdiction` | 可空，普通字符串 | 登记国家／地区，用于解释号码格式和适用规则 |
| `business_license_document_id` | 可空，普通 ID 字段 | 对应 `source_document.id`，营业执照电子件或扫描件 |
| `legal_person_id` | 可空，普通 ID 字段 | 对应 `legal_person.id`，公司类型条件必填 |
| `head_company_id` | 可空，普通 ID 字段 | 对应 `company.id`，分公司类型条件必填 |
| `company_form` | 可空，普通字符串 | 有限责任公司、股份有限公司等；公司类型填写，分公司留空 |
| `contact_phone` / `contact_email` | 可空，普通字符串 | 业务联系电话和邮箱 |
| `business_address` | 可空，普通字符串 | 实际办公地址，与注册地址分别保存 |
| `branch_manager_person_id` | 可空，普通 ID 字段 | 分公司负责人 → `person.id`，与法人法定代表人分别表达 |
| `registered_capital_amount` / `registered_capital_currency` | 可空，金额／字符串 | 公司登记注册资本及币种，分公司不填 |
| `business_term_start_date` / `business_term_end_date` | 可空，日期 | 登记营业期限起止日期 |
| `is_business_term_indefinite` | 可空，布尔值 | 登记长期经营时为真，结束日期留空；未知时为 NULL |
| `established_at` / `terminated_at` | 可空 | 当前公司或分公司的成立／终止日期 |
| `status` | `NOT NULL` | 状态，取值及流转由应用层处理 |

应用层按类型处理：

- `company_type = company`：填写 `legal_person_id`，与法人一对一关联；`head_company_id` 留空。
- `company_type = branch`：填写 `head_company_id`，目标必须是 `company` 类型且不能为自身；`legal_person_id` 和 `company_form` 留空。归属法人通过总公司的 `legal_person_id` 获取。
- 子公司属于 `company` 类型，拥有自己的法人，母子公司关系记录在股东明细中。`head_company_id` 专用于分公司隶属。

两个关联字段都允许数据库 `NULL`，条件必填、类型匹配及一对一规则在应用层处理。

公司对象拥有 `registrations[]`、`bank_accounts[]`、`shareholdings[]` 和 `versions[]`。分公司使用自己的登记、银行账户及版本集合，不建立自身股东明细。

上述证照、税号、注册资本、营业期限、登记负责人和登记地址字段直接展示在公司对象上，值来自当前有效的 `company_registration` 版本。应用层在同一事务中更新登记版本和当前字段，历史以登记版本为准；不形成两套独立编辑的数据。内部 `company_code` 与证照号码、税号分别保存。

对于中国已取得统一社会信用代码的法人和其他组织，纳税人识别号使用该 18 位代码。因此同一家公司的 `unified_social_credit_code` 与 `taxpayer_identification_number` 可以是同一个值，不要求三个字段分别填写不同号码。[国家税务总局《关于修订纳税人识别号代码标准的公告》](https://www.chinatax.gov.cn/chinatax/n810341/n810765/n1465977/n1466017/c1967505/content.html)

号码未知或不适用时保存 `NULL`，不使用空串、零或占位号码。应用层按登记地区、资料和业务状态校验完整性及格式，数据库不增加格式检查或唯一约束。分公司的号码按自身资料填写，不默认继承总公司税号。历史旧税号保存在对应有效期间的登记版本中。

### `company_registration`

每家公司、分公司各自拥有的登记资料版本，明确保存工商登记标识与税号：

- `id` 主键
- `company_id` 普通 ID 字段，`NOT NULL` → `company.id`，记录实际登记的公司或分公司
- `version_number` / `registration_type`
- `unified_social_credit_code` 可空字符串：统一社会信用代码
- `business_license_registration_number` 可空字符串：营业执照注册号／工商注册号
- `taxpayer_identification_number` 可空字符串：该版本适用的纳税人识别号
- `registration_authority` / `registration_jurisdiction`
- `registered_name` / `registered_address` / `registration_status` / `registered_at`
- `business_scope` 可空：登记经营范围
- `registered_capital_amount` / `registered_capital_currency` 可空：登记注册资本及币种
- `business_term_start_date` / `business_term_end_date` / `is_business_term_indefinite` 可空：登记营业期限
- `branch_manager_person_id` 可空 → `person.id`：该登记版本的分公司负责人
- `business_license_document_id` 普通 ID 字段，可空 → `source_document.id`，该版本营业执照材料
- `tax_information_source_document_id` 普通 ID 字段，可空 → `source_document.id`，税号确认或历史税务登记材料
- `valid_from` / `valid_to` / `recorded_at`
- `source_document_id` 普通 ID 字段，可空 → `source_document.id`

公司和分公司分别保存自己的证照号码、税号、名称及状态。一个版本代表该期间完整的登记资料；资料变化时保留旧版本及依据，应用层维护当前有效版本。公司关联的法人读取该公司的登记资料，不再在法人登记集合重复维护同一证照；非公司法人仍使用 `legal_person_registration`。登记标识去重由应用层处理，不把同一公司两个语义字段里的相同号码当作两家公司。

### `company_bank_account`

公司对象拥有的银行账户：`id`、`company_id` → `company.id`、`bank_account_name`、`bank_account_number`、`bank_name`、`bank_branch_name`（可空）、`bank_routing_number`（可空）、`account_currency`、`is_receipt_account`、`is_payment_account`、`valid_from`、`valid_to`、`status`。账号及银行编码使用字符串；公司与分公司各自维护，应用层检查用途和有效期。

付款记录可关联账户主数据，但仍保留实际支付时的账号／户名快照，主数据变化不回写历史付款。自然人或未建档的外部账户可只保留实际单据快照，不伪造公司银行账户。

### `company_shareholding`

`id`、`company_id` → `company.id`；股东 ID 字段组 `shareholder_company_id` → `company.id`、`shareholder_legal_person_id` → `legal_person.id`、`shareholder_person_id` → `person.id`（恰好一个）；`share_class`、`ownership_ratio`、`subscribed_capital_amount`（可空）、`currency`（金额存在时必填）、有效期间、`basis_document_id`（可空）。

应用层检查被投资公司及公司类型股东均为 `company_type = company`；分公司不作为独立股东记录。持股、控制和管理隶属分别表达，不从一条持股记录直接推导控制关系。

### `company_version`

公司对象拥有的版本记录：`id`、`company_id` → `company.id`、`version_number`、`company_code`、`name`、`company_type`、`legal_person_id`、`head_company_id`、`company_form`、`contact_phone`、`contact_email`、`business_address`、成立／终止日期、`status`、有效期间、记录时间和依据文档。

应用层维护版本及当前主记录，按对应版本校验类型与归属。历史单据保留实际参与记录的 ID 和必要快照，归属查询使用业务发生时有效版本，不能用当前总公司或类型覆盖历史事实。

证照号码、税号及登记资料的历史统一由 `company_registration` 保存，`company_version` 不重复保存这组登记字段。

### `person`：自然人

- `id` 主键
- `name`
- `mobile_phone` / `email` 可空：个人联系方式
- `status`：资料有效／停用状态，不能代表员工在职状态

仅保存自然人信息，用于法定代表人、自然人股东和个人收付款等场景。它不是所有对象共同继承的身份表。自然人与员工对象通过明确关联 ID 连接；敏感身份信息是否采集及其访问控制留给技术设计。

### `employee`：员工

- `id` 主键
- `person_id` 普通 ID 字段，`NOT NULL` → `person.id`；应用层维护一对一关系
- `employee_code` 系统员工编号
- `work_email` / `work_phone` 可空：员工工作联系方式
- `status`：员工档案状态，实际入离职和在职期间按雇佣记录查询

员工是独立人事对象，业务中的工时、任职、项目负责人和经办人直接引用 `employee.id`。姓名读取关联自然人信息，避免同一人的姓名在两处独立修改。同一员工可有多段或并行的雇佣记录，离职不删除员工身份。

拥有 `employments[]`、`department_assignments[]` 和 `reporting_lines[]`，详见下一节。

### `department`：部门及内部组织

- `id` 主键
- `company_id` 普通 ID 字段 → `company.id`，部门直接所属的公司或分公司
- `parent_department_id` 普通 ID 字段，可空 → `department.id`
- `department_code` 在直接所属公司记录范围内唯一，由应用层处理
- `name`
- `department_type`：部门、事业部、职能中心、团队等
- `department_head_employee_id` 可空 → `employee.id`：部门负责人
- `department_contact_phone` / `department_contact_email` 可空：部门联系方式
- `valid_from` / `valid_to`
- `status`

部门属于分公司时，`company_id` 直接保存该分公司记录的 ID，总公司通过该记录的 `head_company_id` 获取。行政父部门与子部门属于同一公司记录，层级不能形成循环；集团管理或职能协作不塞入行政父部门字段。

共享部门拥有 `service_companies[]`：`department_service_company` 保存 `id`、`department_id` → `department.id`、`service_company_id` → `company.id`、有效期间和依据文档。部门归属直接记在 `company_id`，服务范围在部门自己的集合里记录；服务对象也可以是分公司记录。

部门名称、归属和行政层级变更保存为拥有的 `department_version`：`id`、`department_id`、`version_number`、上述名称、归属、部门负责人及联系方式字段、有效期间、记录时间、依据文档。部门主记录展示当前有效版本；两者通过同一次变更维护，历史查询以版本为准。

## 员工拥有的任职明细

### `employee_employment`

- `id` 主键
- `employee_id` 普通 ID 字段 → `employee.id`
- 用人对象 ID 字段组：`employer_company_id` → `company.id`、`employer_legal_person_id` → `legal_person.id`，恰好一个
- `employee_number` 在所选用人对象范围内唯一
- `employment_type`：全职、兼职等用工类型，应用层解释
- `employment_contract_number` 可空：劳动／聘用合同编号
- `employment_start_date`：实际入职日期
- `employment_end_date` 可空：雇佣终止生效日期，自该日起不在该雇佣期间内
- `termination_reason` 可空：离职原因
- `employment_contract_start_date` / `employment_contract_end_date` 可空：约定用工合同期限
- `recorded_at`：本条雇佣资料录入时间；实际雇佣有效期间直接使用入职及终止生效日期，不重复保存一套期间
- `status`
- `employment_document_id` 普通 ID 字段，可空 → `source_document.id`

按实际用工依据填写用人对象，不根据工资付款方、所在部门或项目交付公司推断。

### `employee_department_assignment`

- `id` 主键
- `employee_id` 普通 ID 字段 → `employee.id`
- `employment_id` 普通 ID 字段 → `employee_employment.id`
- `department_id` 普通 ID 字段 → `department.id`
- `assignment_type`：主岗、兼岗、矩阵任职等
- `position_title` 可空
- `valid_from` / `valid_to`
- `status`

任职的雇佣记录必须属于同一员工，任职期间不得超出雇佣有效期间。跨公司任职可记录；员工的用人对象仍以雇佣记录为准。

### `employee_reporting_line`

- `id` 主键
- `employee_id` 普通 ID 字段 → `employee.id`
- `assignment_id` 普通 ID 字段 → `employee_department_assignment.id`
- `manager_assignment_id` 普通 ID 字段 → `employee_department_assignment.id`
- `reporting_type`：行政、职能等
- `valid_from` / `valid_to`

该员工必须是 `assignment_id` 对应员工；汇报期间应落在双方任职有效期间内，禁止向自身任职汇报。行政汇报链防止循环，职能汇报按独立类型记录。

## 供应商管理

### `supplier`：供应商档案

独立记录采购合作管理信息。一条档案表示一个采购公司或分公司对一个实际供货对象的管理；公司、法人、自然人的基础资料继续保存在各自对象。供应商拥有自己的编号、准入及合作生命周期，不使用统一 Party 或通用关系表。

| 字段 | 要求与含义 |
|---|---|
| `id` | 主键 |
| `supplier_code` | `NOT NULL`，供应商编号；在采购公司范围内由应用层去重 |
| `supplier_name` | `NOT NULL`，采购管理显示名称；登记名称和签约名称从基础对象及单据快照读取 |
| `purchasing_company_id` | `NOT NULL`，普通 ID → `company.id`；本档案所属采购公司或分公司 |
| `company_id` / `legal_person_id` / `person_id` | 可空，分别 → `company.id` / `legal_person.id` / `person.id`；确认档案时恰好一个，表示实际供货对象 |
| `procurement_owner_employee_id` | 可空 → `employee.id`；采购管理负责人，准入通过时必填 |
| `admission_status` | `NOT NULL`，待提交、审核中、通过、驳回；与合作状态分别维护 |
| `admission_submitted_at` | 可空，准入提交时间 |
| `admission_reviewed_by_employee_id` / `admission_reviewed_at` | 可空 → `employee.id`／时间；准入审核完成时必填 |
| `admission_rejection_reason` | 可空，准入驳回时必填 |
| `admission_document_id` | 可空 → `source_document.id`；准入审批依据 |
| `cooperation_status` | `NOT NULL`，未启用、合作中、暂停、终止 |
| `cooperation_started_on` / `cooperation_ended_on` | 可空，实际合作起止日期，发生相应事项时填写 |
| `cooperation_status_effective_at` | `NOT NULL`，当前合作状态生效时间 |
| `cooperation_status_reason` | 可空，暂停／终止／恢复合作时必填 |
| `default_settlement_currency` | 可空，默认结算币种 |
| `default_payment_due_basis` | 可空，发票确认日、验收日、约定固定日、合同另定；仅表示默认付款到期规则 |
| `default_payment_term_days` | 可空，从默认基准日起计算的自然日数；采用发票确认日或验收日基准时填写 |
| `default_payment_day_of_month` | 可空，采用每月固定日时填写 1～31；短月取月末 |

默认条件仅用于拟订合同时的初始值；合同签订后以合同收付款计划为准，不因供应商档案变更重算历史到期日。固定日约定仍须在具体合同计划中明确首个付款月份和日期。

同一实际供货对象可分别成为不同采购公司的供应商；同一采购公司与同一供货对象原则上维护一份档案，分类放在所属明细中。已被业务引用的采购公司和供货对象 ID 不允许换成其他对象；签约主体变化新建档案，历史档案保留。公司供应商使用 `company_id`，不能再以其对应法人重复建档。

供应商拥有 `contacts[]`、`supply_categories[]`、`qualifications[]` 和 `versions[]`：

- `supplier_contact`：`id`、`supplier_id` → `supplier.id`、`person_id` → `person.id`、`contact_role`（商务、交付、财务等）、`business_phone`、`business_email`、`is_primary`、有效期间、`status`。电话和邮箱可空，但有效联系人至少提供一种联系方式；同一角色同一期间最多一个主联系人，由应用层校验。
- `supplier_supply_category`：`id`、`supplier_id` → `supplier.id`、`supply_category_code`、`supply_category_name`、`supply_scope_description`、有效期间、`status`。明确可供应的商品／服务分类及范围，同一档案可有多项。
- `supplier_qualification`：`id`、`supplier_id` → `supplier.id`、`qualification_name`、`certificate_number`（可空）、`issuing_authority`（可空）、`issued_on`（可空）、`expires_on`（可空）、`has_no_expiry`、`qualification_document_id` → `source_document.id`、`review_status`、`reviewed_by_employee_id` → `employee.id`（可空）、`reviewed_at`（可空）、`rejection_reason`（可空）、`status`。记录供货所需资质及审核，明确长期有效时 `has_no_expiry` 为真且到期日留空；未掌握到期日不能当作长期有效。公司营业执照和税号仍归公司登记资料，不在资质明细中再次维护。
- `supplier_version`：`id`、`supplier_id` → `supplier.id`、`version_number`、与供应商相同的显示名称、采购负责人、准入状态及审核字段、合作状态及其日期／原因、默认结算字段、有效期间、`recorded_at`、`changed_by_employee_id` → `employee.id`、`change_reason`、`basis_document_id` → `source_document.id`（可空）。初始建档即建版本，状态或条件变化增加版本，应用层在同一事务内维护当前字段；联系人、分类、资质各自保留期间和更正记录。

新增采购承诺时按当时的准入、合作状态和适用资质判断；暂停或终止不能删除已发生费用、未结应付或阻止有依据的存量结算。对停用供应商的后续交付按原合同及处理依据记录。供应商账户由其基础对象维护；本期公司供应商可使用 `company_bank_account`，非公司供应商的付款账户仍以具体付款账户快照记录。第三方收款必须记录实际收款方及代收依据，不能通过改供应商身份实现。

### 供应商在业务中的直接关联

- 采购合同使用 `procurement_company_id` 和 `supplier_id`，直接记录该采购公司所用档案；采购公司等于档案的 `purchasing_company_id` 且与买方公司一致，卖方对象与档案供货对象一致。合同管理公司可与采购公司不同。
- 外部费用使用可空的 `supplier_id`，同时保留实际供货对象字段。已有档案的采购费用填写该 ID；临时差旅消费等可以只记录实际供货对象，不为一次性消费强制建档。填写档案时两者必须一致；成本承担对象仍独立记录。
- 供应商应付项使用可空的 `supplier_id`；填写时债权人对应档案供货对象，债务公司对应档案采购公司。员工报销应付不填写该字段，原始商户从费用追溯。
- 合同、费用和应付项关联同一采购业务时档案一致；跨公司采购承担或债务转移须另记对应合同／应付事实及依据，不能改写原档案归属。
- 发票保存实际开票方，付款保存实际收款方。不能从供应商 ID 推断这两个角色，也不能自动把默认条件当成实际结算义务。

## 项目与合同

### `project`

项目是管理和成本归集对象。

- `id` 主键
- `project_code` / `name`
- `project_type`：客户交付、内部建设等
- `project_scope_description`：项目范围
- `actual_start_at` / `actual_completed_at` 可空：实际启动／完成时间
- `closed_at` 可空：管理结项时间
- `budget_currency` 可空：项目预算币种
- `approved_budget_version_id` 可空 → `project_budget_version.id`：当前已批准预算版本
- 客户 ID 字段组：`customer_company_id`、`customer_legal_person_id`、`customer_person_id`，分别指向对应基础对象，可选、最多一个
- `owner_company_id` 普通 ID 字段 → `company.id`，项目管理公司
- `lead_department_id` 普通 ID 字段，可空 → `department.id`
- `project_manager_employee_id` 普通 ID 字段，可空 → `employee.id`
- `planned_start_at` / `planned_end_at`
- `status`

客户与管理公司直接记录。预算是计划，不由合同金额或实际成本自动替代。尚未明确客户或内部项目可不填客户。合同签约方以合同本身为准，避免项目重复维护多个合同的签约事实。

拥有 `delivery_companies[]`、`support_departments[]`、`contracts[]`：

- `project_delivery_company`：`id`、`project_id` → `project.id`、`delivery_company_id` → `company.id`、`lead_department_id` → `department.id`（可空）、`responsibility_description`、有效期间、`basis_document_id`（可空）。支持一家或多家公司执行交付。
- `project_support_department`：`id`、`project_id` → `project.id`、`department_id` → `department.id`、`responsibility_description`、有效期间。
- `project_contract`：`id`、`project_id` → `project.id`、`contract_id` → `contract.id`、`relationship_note`（可空）；`project_id, contract_id` 唯一。

### `project_member`

项目拥有的成员明细：`id`、`project_id` → `project.id`、`employee_id` → `employee.id`、`employment_id` → `employee_employment.id`、`department_assignment_id`（可空）→ `employee_department_assignment.id`、`project_role`（如交付负责人、设计、实施）、`planned_hours`（可空）、`valid_from`、`valid_to`。员工及雇佣、任职需一致；是否限定成员才能填工时由项目流程设置，不能仅凭没有历史工时就认定不是成员。

### `project_budget_version`

项目拥有的预算版本：`id`、`project_id` → `project.id`、`version_number`、`budget_currency`、`planned_labor_hours`、`labor_cost_budget_amount`、`nonlabor_cost_budget_amount`、`budget_cost_basis`（标准人工或工资成本口径）、`approval_status`、`submitted_at`、`approved_by_employee_id`（可空）→ `employee.id`、`approved_at`（可空）、`valid_from`、`valid_to`、`basis_document_id`（可空）。预算总额为人工与非人工预算之和；未知分类金额在草稿可空，不能用零代替未编制。

项目当前批准版本 ID 由应用层维护，并检查版本归属及审批状态。实际成本与预算对比时使用相同币种及人工成本口径，不把标准人工与工资成本同时加总。

### `project_version`

项目拥有的管理信息版本：`id`、`project_id` → `project.id`、`version_number`、与项目相同的客户 ID 字段组、`owner_company_id`、`lead_department_id`、`project_manager_employee_id`、`project_type`、`project_scope_description`、名称及计划日期、有效期间、记录时间、`basis_document_id`（可空）。关联目标及应用层单选规则与项目一致。

客户、管理公司、负责人变更保留版本，项目主记录展示当前有效版本，并在同一次变更中维护。交付及支持分工在各自明细内保留期间和更正版本；已发生业务不按当前角色回填。

### `contract`

- `id` 主键
- `contract_number` / `contract_type`
- `contract_title`：合同名称
- `managing_company_id` 可空 → `company.id`：负责合同管理的公司或分公司，不替代实际签约方
- `procurement_company_id` 可空 → `company.id`：本系统管理采购业务的买方公司或分公司，与供应商档案配套填写
- `supplier_id` 可空 → `supplier.id`：采购管理所用供应商档案；纳入供应商管理的采购合同确认时必填
- `contract_owner_employee_id` 可空 → `employee.id`：合同经办负责人
- `buyer_signatory_person_id` / `seller_signatory_person_id` 可空 → `person.id`：实际签署人
- `buyer_name_snapshot` / `seller_name_snapshot`：签约时主体名称
- `buyer_taxpayer_id_snapshot` / `seller_taxpayer_id_snapshot` 可空：签约资料中的税号
- `buyer_contact_person_id` / `seller_contact_person_id` 可空 → `person.id`：合同联系人
- 买方 ID 字段组：`buyer_company_id`、`buyer_legal_person_id`、`buyer_person_id`，恰好一个
- 卖方 ID 字段组：`seller_company_id`、`seller_legal_person_id`、`seller_person_id`，恰好一个
- `signed_at`
- `valid_from` / `valid_to`
- `currency`
- `pricing_method`：固定总价、按量、按工时等，应用层解释
- `contract_net_amount` / `contract_tax_amount` / `contract_gross_amount` 可空：约定不含税金额、税额、含税总价
- `approval_status`：合同审批状态，与合同生效／终止状态分别记录
- `contract_approved_at` / `contract_approved_by_employee_id` 可空 → 时间／`employee.id`
- `termination_date` / `termination_reason` 可空：提前终止的日期及原因
- `status`
- `source_document_id` 普通 ID 字段 → `source_document.id`

含税、不含税与税额按合同实际约定填写，已知时核对 `contract_gross_amount = contract_net_amount + contract_tax_amount`。固定含税总价合同可先只填含税总价，未知税额不能按零处理；按量或按工时合同的未定总价留空，按合同品项计价。税率按品项记录，不假定整份合同只有一种税率。

每个关联 ID 指向说明中的具体对象 `id`。第一版买方／卖方也表示服务购买方／提供方；角色按实际合同类型解释。采购合同通过 `supplier_id` 记录供应商管理档案，并保留卖方实际对象；销售合同中的买方表示该合同的客户。供应商管理身份与实际交易对象分别表达。

多方合同拥有 `additional_signatories[]`：`contract_additional_signatory` 保存 `id`、`contract_id` → `contract.id`、`signatory_company_id`／`signatory_legal_person_id`／`signatory_person_id`（恰好一个）、`signatory_role`、责任说明、有效期间和变更依据。主合同的买卖双方不在此重复记录。经办员工、授权代表如有需要，使用专门的员工／自然人字段。

### `contract_term_version`

合同拥有的条款版本：`id`、`contract_id` → `contract.id`、`version_number`、`amendment_id` → `contract_amendment.id`（可空，原始条款为空）、与合同相同的买方／卖方 ID 字段组、`currency`、`pricing_method`、`contract_net_amount`、`contract_tax_amount`、`contract_gross_amount`、买卖方名称及税号快照、签署人和联系人 ID、合同生效及终止日期、版本有效期间、记录时间。

条款版本还保存 `procurement_company_id` → `company.id`、`supplier_id` → `supplier.id`，可空规则及关联一致性与合同相同。

原合同建立初始版本；签约对象、供应商关联、总价或合同期间改变时建立新版本。关联目标及应用层单选规则与合同一致，变更依据必须属于该合同。应用层维护同一时间当前有效版本唯一，合同主记录展示当前有效条款并在同一次变更中维护，历史查询以版本为准。

### `contract_line` 与 `contract_line_term_version`

合同拥有的品项：`contract_line` 保存 `id`、`contract_id` → `contract.id`、`line_number`。品项的计价及范围在 `contract_line_term_version` 保存：`id`、`contract_line_id` → `contract_line.id`、`contract_term_version_id` → `contract_term_version.id`、`item_name`、`item_specification`（可空）、`unit`、`contracted_quantity`（可空）、`unit_net_price`（可空）、`tax_rate`（可空）、`line_net_amount`／`line_tax_amount`／`line_gross_amount`（可空）、有效期间。

一个版本内的品项属于同一合同且币种沿用合同版本；单价明确为不含税单价，小时类服务以小时为单位。折扣后的约定金额直接体现在该版本品项金额中，保留变更依据。混合税率合同按明细核对总价；数量未定时不虚构数量或总价。

### `contract_amendment`

合同拥有的变更记录：`id`、`contract_id` → `contract.id`、`amendment_number`、`signed_at`、`effective_at`、`source_document_id` → `source_document.id`、`summary`。变更后的条款指向变更依据，历史条款及业务事实保持可查。合同签约方发生变更时同样保存条款版本，不能覆盖原买卖方后改写历史单据。

### `contract_stage`

- `id` 主键
- `contract_id` 普通 ID 字段 → `contract.id`
- `stage_code` / `sequence_no`
- `stage_name`：阶段名称
- `responsible_employee_id` 可空 → `employee.id`：阶段负责人
- `status`

阶段身份与条款版本分开，阶段编号在合同内唯一。

### `contract_stage_term_version`

- `id` 主键
- `contract_stage_id` 普通 ID 字段 → `contract_stage.id`
- `version_number`
- `amendment_id` 普通 ID 字段，可空 → `contract_amendment.id`；空表示原始条款
- `stage_name` / `deliverable_description` / `acceptance_criteria`：该条款版本的阶段名称、交付说明及验收条件
- `planned_start_at` / `planned_due_at`
- `stage_net_amount` / `stage_tax_amount` / `stage_gross_amount` 可空：该阶段约定价款
- `currency`
- `valid_from` / `valid_to`
- `recorded_at`

同一阶段当前有效条款唯一，变更依据必须属于同一合同。阶段价款分别按不含税、税额、含税口径与合同总价核对，三者已知时含税等于不含税加税额，不表示开票、收款或收入。

### `contract_stage_deliverable`

阶段条款版本拥有的交付项：`id`、`stage_term_version_id` → `contract_stage_term_version.id`、`contract_line_term_version_id`（可空）→ `contract_line_term_version.id`、`deliverable_code`、`deliverable_name`、`deliverable_specification`、`unit`、`planned_quantity`、`acceptance_criteria`、`planned_delivery_date`、`planned_acceptance_date`（可空）。不可按物理数量计量的报告或服务成果可用“项”并约定数量 1；不能把主观百分比当作已验收数量。变更后建立新版本的交付项，原提交继续引用原交付项。同一阶段同一交付项跨版本保持 `deliverable_code` 稳定；累计验收按阶段与该代码汇总，不能因新版本生成新 ID 而重复计入。交付项增减或拆分须明确分配已有验收量，不自动清零。

### `contract_billing_schedule`：开票计划

合同拥有的开票计划：`id`、`contract_id` → `contract.id`、`contract_term_version_id` → `contract_term_version.id`、`contract_stage_id`（可空）→ `contract_stage.id`、`schedule_number`、`billing_trigger`（指定日期、签约、阶段验收、人工确认）、`planned_invoice_gross_amount`、`currency`、`planned_invoice_date`（可空）、`days_after_trigger`（可空）、`trigger_description`（补充条款说明）、`valid_from`、`valid_to`、`supersedes_billing_schedule_id`（可空）→ `contract_billing_schedule.id`。

### `contract_payment_schedule`：收付款计划

合同拥有的收付款计划：`id`、`contract_id` → `contract.id`、`contract_term_version_id` → `contract_term_version.id`、`contract_stage_id`（可空）→ `contract_stage.id`、`schedule_number`、`installment_name`、`payment_purpose`（预付款、进度款、尾款、质保金等）、`payment_trigger`（指定日期、签约、阶段验收、开票、人工确认）、`planned_payment_amount`、`currency`、`planned_due_date`（可空）、`days_after_trigger`（可空）、`trigger_description`（补充说明）、`valid_from`、`valid_to`、`supersedes_payment_schedule_id`（可空）→ `contract_payment_schedule.id`。

开票计划和资金计划分别记录。款项方向按合同买卖双方解释；金额为计划结算金额，不能作为收入。指定日期触发时填对应计划日期；相对事件触发时填日历天数及关联阶段，实际触发事件和最终到期日保存在应收／应付项中。复合条件使用人工确认并保留依据，未定义自动规则时不能靠解析说明文本直接发起资金操作。

两类计划的合同、阶段及版本必须一致。原计划作废或被替代时保留记录，已发生的发票、应收应付和付款不被覆盖。计划金额、实际债权债务、开票金额、到账金额分别核对。

## 履约、工时与成本

### `work_log`

- `id` 主键
- `employee_id` 普通 ID 字段 → `employee.id`
- `employment_id` 普通 ID 字段 → `employee_employment.id`
- `department_assignment_id` 普通 ID 字段，可空 → `employee_department_assignment.id`
- `project_id` 普通 ID 字段 → `project.id`
- `contract_stage_id` 普通 ID 字段，可空 → `contract_stage.id`
- `work_date` / `hours`：工作日期及投入小时数
- `work_description`：本次具体工作内容
- `is_billable`：是否可按合同计费，与人工成本是否归集分别表达
- `submitted_at` 可空：提交审批时间
- `approval_status`：草稿、待审、通过、驳回等
- `record_status`：有效、已被替代、已作废，与审批状态分开
- `approved_by_employee_id` 可空 → `employee.id`：审批员工
- `approved_at` / `approval_comment` 可空：审批时间及意见
- `supersedes_work_log_id` 可空 → `work_log.id`：更正的原工时记录

工时直接引用员工；雇佣及任职必须属于该员工，工时日期在对应有效期间内。所选阶段的合同必须包含在该项目的 `contracts[]` 中。时长大于零，已批准工时更正保留原记录及更正依据；应用层防止同一替代链上新旧工时同时计入。可计费工时并不直接生成收入或发票。

### `labor_cost_rate`

人工成本口径的版本记录。

- `id` 主键
- `employment_id` 普通 ID 字段 → `employee_employment.id`
- `cost_basis`：标准成本、工资成本等
- `hourly_cost_amount` / `currency`
- `rate_version_number`
- `valid_from` / `valid_to`
- `approval_status`
- `approved_by_employee_id` 可空 → `employee.id`
- `approved_at` 可空
- `basis_document_id` 普通 ID 字段，可空 → `source_document.id`

同一雇佣和成本口径的有效期间不重叠。成本率来源和计算口径需由业务确定，不能把工时数直接当金额。

### `cost_occurrence`

原始费用或人工成本事实，金额只记录一次。

- `id` 主键
- 成本承担对象 ID 字段组：`bearing_company_id`、`bearing_legal_person_id`，恰好一个，指向对应对象
- `incurred_by_employee_id` 普通 ID 字段，可空 → `employee.id`，记录经办／发生费用的员工
- 供应商 ID 字段组：`supplier_company_id`、`supplier_legal_person_id`、`supplier_person_id`，可空且最多一个
- `supplier_id` 可空 → `supplier.id`：采购合作档案；上面的字段组仍记录实际供货对象，填写档案时必须与其一致
- `cost_number`：费用记录编号
- `purchase_contract_id` 可空 → `contract.id`：实际采购依据
- `expense_start_date` / `expense_end_date` 可空：费用覆盖的服务或差旅期间
- `expense_net_amount` / `expense_tax_amount` / `expense_gross_amount` 可空：原始支出的不含税、税额、含税金额
- `employee_advanced_amount` 可空：经办员工实际垫付金额
- `cost_policy_code` / `cost_policy_version`：归集口径及版本标识，不存计算脚本
- `cost_type` / `incurred_at`
- `cost_amount`：该笔费用或人工成本的原始金额
- `currency`：该笔成本金额的币种
- `cost_basis`：实际费用、标准人工成本、工资成本等
- `work_log_id` 普通 ID 字段，可空 → `work_log.id`
- `labor_cost_rate_id` 普通 ID 字段，可空 → `labor_cost_rate.id`
- `costed_hours` / `hourly_cost_amount_snapshot` 可空：本次计入的工时及每小时成本快照
- `payroll_employee_id` 可空 → `employee.id`：工资成本对应员工
- `payroll_employment_id` 可空 → `employee_employment.id`
- `payroll_period` / `payroll_batch_number` / `payroll_line_number` 可空：工资来源期间、批次和行号
- `approval_status` / `approved_by_employee_id` / `approved_at`：审批状态、员工 ID 和时间，未审批时后两项可空
- `supersedes_cost_occurrence_id` 可空 → `cost_occurrence.id`：更正的原成本记录
- `source_document_id` 普通 ID 字段，可空 → `source_document.id`
- `description` / `status`

按工时计算人工成本时，工时、成本率、计入小时数和成本单价快照同时必填；日期、雇佣关系和币种匹配。`cost_amount` 按该口径由计入小时数乘单价并舍入得到，累计计入小时数不超过有效已批工时。工资导入时员工、雇佣、期间、批次及行号必填，以该来源组合在应用层防止重复导入；不强制使用工时成本率。

外部费用确认时明确供应商和实际发生期间；员工费用申报可先作为草稿，核实后确认。`expense_gross_amount = expense_net_amount + expense_tax_amount` 在三者已知时核对；`cost_amount` 是按指定口径可归集的成本，不能直接等同于含税支出或付款。税额如何计入成本由业务采用的政策决定，本模型不预设处理。员工垫付额是资金事实，报销不再生成一笔重复成本。

同一人工投入在同一核算口径下只能计入一次；标准成本和工资成本并存时，报表明确选择口径。已发生费用不因付款或项目分配而重复创建。

### `cost_invoice_line`

费用拥有的发票依据明细：`id`、`cost_occurrence_id` → `cost_occurrence.id`、`invoice_line_id` → `invoice_line.id`、`matched_expense_gross_amount`、`currency`。一笔费用可关联多行票，一行票可支持多笔费用，应用层按同币种含税支出口径及原票／更正票符号检查匹配上限，不把它当作成本分配或付款核销。无发票费用仍可保留其他业务依据材料，是否准予报销及入账由流程决定。

### `expense_claim` 与 `expense_claim_line`

员工报销单：`id`、`claim_number`、`claimant_employee_id` → `employee.id`、`reimbursement_company_id` → `company.id`（承担报销义务的公司）、`currency`、`submitted_at`、`approval_status`、`approved_by_employee_id`（可空）→ `employee.id`、`approved_at`（可空）、`approval_comment`（可空）、`source_document_id`（可空）。

报销单拥有的明细：`expense_claim_line` 保存 `id`、`expense_claim_id` → `expense_claim.id`、`line_number`、`cost_occurrence_id` → `cost_occurrence.id`、`claimed_reimbursement_amount`、`approved_reimbursement_amount`（审批前可空）、`rejection_reason`（可空）。申请金额、批准金额与成本金额各自表达，报销合计从有效明细汇总。同一员工同一垫付款不能在多个已批准报销单重复报销。

报销形成员工应付款 `payable_item`，债务人按承担报销义务的公司记录；实际支付通过付款核销关联该应付项，实际付款公司可以是有依据的代付方。公司直接向供应商支付的费用不同时形成相同金额的员工应付款；部分员工垫付、部分公司付款分别记录实际义务。

### `cost_allocation`

费用对象拥有的 `allocations[]`：

- `id` 主键
- `cost_occurrence_id` 普通 ID 字段 → `cost_occurrence.id`
- `project_id` 普通 ID 字段 → `project.id`
- `contract_stage_id` 普通 ID 字段，可空 → `contract_stage.id`
- `allocated_amount` / `currency`
- `allocation_period` / `allocation_method`：归集期间及按工时、比例、直接指定等方法
- `allocation_basis_hours` 可空：按工时分配使用的小时数
- `allocation_ratio` 可空：按比例分配使用的比例
- `allocated_by_employee_id` 可空 → `employee.id`
- `allocated_at`：本次归集时间
- `supersedes_cost_allocation_id` 可空 → `cost_allocation.id`：替代的原分配记录
- `basis_document_id` 普通 ID 字段，可空 → `source_document.id`
- `status`

统一从已有金额的成本事实分配。工时人工成本先生成可追溯的 `cost_occurrence`，再分配，避免从无金额的工时来源校验分配上限。阶段必须属于项目关联的合同；有效分配合计不得超过来源可分配金额，币种一致。跨法人执行的管理归集不自动形成内部交易或另一家公司的账务成本。按工时生成的单项成本只能归集到该工时所属项目及适用阶段；工资汇总成本可按有据可查的工时或比例分配。成本或分配更正时同步处理受影响的有效分配及已过账调整。

### `delivery_event`

- `id` 主键
- `project_id` 普通 ID 字段 → `project.id`
- `contract_stage_id` 普通 ID 字段 → `contract_stage.id`
- `stage_term_version_id` 普通 ID 字段 → `contract_stage_term_version.id`
- 提交对象 ID 字段组：`submitter_company_id`、`submitter_legal_person_id`、`submitter_person_id`，恰好一个
- `submitted_by_employee_id` 普通 ID 字段，可空 → `employee.id`
- `delivery_number`：交付提交编号
- `submitted_at` / `deliverable_description`
- `resubmission_of_delivery_event_id` 可空 → `delivery_event.id`：返工后重新提交的原交付
- `status`：已提交、已撤回等；验收结果另行保存
- `source_document_id` 普通 ID 字段，可空 → `source_document.id`

记录实际交付及所依据的条款版本；阶段、版本和项目合同关联必须一致。一个阶段可有多次提交。

### `delivery_line`

交付拥有的明细：`id`、`delivery_event_id` → `delivery_event.id`、`stage_deliverable_id` → `contract_stage_deliverable.id`、`submitted_quantity`、`unit`、`delivered_artifact_version`（可空）、`deliverable_document_id`（可空）→ `source_document.id`。交付项须属于该提交所引用的阶段条款版本，数量和单位以该交付项为准；重提记录保留版本及原提交关联，不能累计重复计算已完成数量。

### `acceptance_event`

- `id` 主键
- `delivery_event_id` 普通 ID 字段 → `delivery_event.id`
- 验收对象 ID 字段组：`acceptor_company_id`、`acceptor_legal_person_id`、`acceptor_person_id`，恰好一个
- `accepted_by_employee_id` 普通 ID 字段，可空 → `employee.id`
- `accepted_by_person_id` 可空 → `person.id`：外部验收签署人；与员工经办字段按实际选填，若同时填写须对应同一人
- `acceptance_number`：验收单编号
- `supersedes_acceptance_event_id` 可空 → `acceptance_event.id`：更正的原验收结论
- `status`：有效、已撤销等，决定哪些明细计入验收汇总
- `accepted_at`：本次验收时间
- `acceptance_result`：本次验收结果，如通过、不通过、部分通过，由应用层解释
- `acceptance_comments` 可空：验收意见，补充说明结果
- `source_document_id` 普通 ID 字段，可空 → `source_document.id`

验收结果与复验分别保留。验收方组织、内部员工和外部签署人分别记录。交付、验收、开票和收入确认使用各自记录及状态。

### `acceptance_line`

验收拥有的明细：`id`、`acceptance_event_id` → `acceptance_event.id`、`delivery_line_id` → `delivery_line.id`、`reviewed_quantity`、`accepted_quantity`、`rejected_quantity`、`unit`、`rejection_reason`（可空）。本次验收数量等于通过加不通过数量；复验对先前未通过部分重新记录，不能重复确认已通过数量。

同一有效交付项累计通过量不得超过其提交量；同一合同阶段交付项跨提交的累计通过量不得超过约定数量。更正时仅有效结论计入；头部“部分通过”可从本次明细得到。阶段全部交付项满足验收条件后才完成履约，不能仅凭一条验收头记录判断整个阶段完成。

## 单据与资金收付

### `source_document`

来源材料登记对象，不承载主体共同身份。

- `id` 主键
- `document_type` / `document_number` 可空
- 出具对象 ID 字段组：`issuer_company_id`、`issuer_legal_person_id`、`issuer_person_id`，可选、最多一个
- `prepared_by_employee_id` 普通 ID 字段，可空 → `employee.id`
- `issued_at` 可空
- `file_storage_uri` 可空：来源材料文件的存储位置
- `file_name` / `mime_type` / `file_checksum` 可空：材料文件名、格式及完整性摘要
- `uploaded_at` / `uploaded_by_employee_id` 可空：上传时间及员工 ID
- `content_summary` 可空：材料内容摘要
- `status`

合同、发票、报销、验收证明等各自业务表直接引用材料。材料中的名称及登记号可作为签发时的快照保存，不因主数据改名而变化。

### `invoice`

- `id` 主键
- `invoice_number`：发票号码
- `invoice_code` 可空：票面存在时保存发票代码
- `invoice_type`：发票种类，按实际票面记录
- `invoice_medium`：电子、纸质等
- `invoice_color`：蓝字、红字等票面类别
- `invoice_net_amount` / `invoice_tax_amount` / `invoice_gross_amount`：票面不含税、税额、含税合计
- `original_invoice_id` 可空 → `invoice.id`：红字或更正票关联原发票
- `invoice_verified_at` / `verified_by_employee_id` 可空：票面核对时间和员工 ID
- `verification_result` / `verification_comment` 可空：核对结果及差异说明；头部与明细金额差异由各自明确金额字段计算
- `voided_at` / `void_reason` 可空：作废时间及原因
- 开票方 ID 字段组：`issuer_company_id`、`issuer_legal_person_id`，恰好一个
- 受票方 ID 字段组：`recipient_company_id`、`recipient_legal_person_id`、`recipient_person_id`，恰好一个
- `issuer_name_snapshot` / `issuer_taxpayer_id_snapshot`：票面开票方名称、纳税人识别号
- `recipient_name_snapshot` / `recipient_taxpayer_id_snapshot`：票面受票方名称、纳税人识别号；个人等无票面税号时税号字段可空
- `issued_at` / `currency` / `status`
- `source_document_id` 普通 ID 字段 → `source_document.id`

第一版开票方范围为上述组织类型；新增自然人等开票业务时再扩展专属字段。开票方、受票方在发票上直接记录，不从合同或付款方复制推断。名称及税号快照保存实际票面值；开票准备时可从当前主数据预填，已开具或导入的发票不能随主数据变化而被改写。票面税号与当前税号不一致时保留事实及核对结果，不自动覆盖。`invoice_number` 是发票号码，与纳税人识别号分别保存。

### `invoice_line`

发票拥有的 `lines[]`：`id`、`invoice_id` → `invoice.id`、`line_number`、`item_name`、`item_specification`（可空）、`tax_classification_code`（可空）、`quantity`／`unit`（票面未列时可空）、`unit_net_price`（可空）、`tax_rate`（可空）、`tax_treatment`（按票面记录免税、不征税、适用税率等）、`net_amount`／`tax_amount`／`gross_amount`、`original_invoice_line_id`（可空）→ `invoice_line.id`。

税率未知、免税与零税率不靠同一个零值猜测。含税金额等于不含税金额加税额；头部票面合计与明细核对，舍入差异保留核对说明。金额按票面符号保存；红字更正保留原票／原行关联，原票不被删除。开票业务分配沿用票面金额符号并按原票可更正余额核对。发票更正不自动更改债权债务或收入，另由实际业务依据处理。

### `invoice_line_allocation`

发票行拥有的 `allocations[]`：

- `id` 主键
- `invoice_line_id` 普通 ID 字段 → `invoice_line.id`
- `contract_id` 普通 ID 字段，可空 → `contract.id`
- `project_id` 普通 ID 字段，可空 → `project.id`
- `contract_stage_id` 普通 ID 字段，可空 → `contract_stage.id`
- `billing_schedule_id` 普通 ID 字段，可空 → `contract_billing_schedule.id`
- `receivable_item_id` / `payable_item_id` 可空 → `receivable_item.id` / `payable_item.id`：开票对应的结算项，最多一个
- `allocated_amount` / `currency`
- `allocation_basis`：含税、不含税

至少指定合同、项目、阶段、开票计划或结算项之一。阶段、计划及合同必须一致；同时指定项目时，该合同须在项目合同集合内。同一发票行采用一种分配口径；按该口径核对分配上限，不能通过同时建立含税和不含税两组分配重复占用来源。支持一行覆盖多个阶段及一阶段多次开票。关联应收／应付项时固定使用含税口径，合同、币种和实际交易对手须匹配。该明细表示开票覆盖，不计入收付款结算余额。

### `receivable_item`：应收项

- `id` / `receivable_number`
- 债权归属 ID 组：`creditor_company_id`、`creditor_legal_person_id`，恰好一个
- 债务人 ID 组：`debtor_company_id`、`debtor_legal_person_id`、`debtor_person_id`，恰好一个
- `contract_id` / `contract_payment_schedule_id` 可空 → `contract.id` / `contract_payment_schedule.id`
- `project_id` / `contract_stage_id` 可空 → `project.id` / `contract_stage.id`
- `receivable_purpose`：预收约定款、阶段款、尾款、质保金等
- `receivable_amount` / `currency`：已确认应收结算本金及币种
- `confirmed_at` / `confirmed_by_employee_id` → 时间／`employee.id`
- `due_date`：实际应收款到期日；尚待合同条件确定时可空，不能推定为开票日
- `trigger_acceptance_event_id` / `trigger_invoice_id` 可空 → `acceptance_event.id` / `invoice.id`：实际触发依据
- `source_document_id` → `source_document.id`：确认依据
- `status`：草稿、已确认、已作废；结清及逾期按余额和到期日计算

### `payable_item`：应付项

- `id` / `payable_number`
- 债务归属 ID 组：`debtor_company_id`、`debtor_legal_person_id`，恰好一个
- 债权人 ID 组：`creditor_company_id`、`creditor_legal_person_id`、`creditor_person_id`，恰好一个
- `supplier_id` 可空 → `supplier.id`：供应商应付所属采购档案；员工报销及其他非供应商应付留空
- `contract_id` / `contract_payment_schedule_id` 可空 → `contract.id` / `contract_payment_schedule.id`
- `project_id` / `contract_stage_id` 可空 → `project.id` / `contract_stage.id`
- `cost_occurrence_id` 可空 → `cost_occurrence.id`
- `expense_claim_id` 可空 → `expense_claim.id`：员工报销来源
- `payable_purpose`：采购款、预付约定款、员工报销、尾款等
- `payable_amount` / `currency`：已确认应付结算本金及币种
- `confirmed_at` / `confirmed_by_employee_id` → 时间／`employee.id`
- `due_date` 可空：实际应付款到期日
- `trigger_acceptance_event_id` / `trigger_invoice_id` 可空 → `acceptance_event.id` / `invoice.id`
- `source_document_id` → `source_document.id`
- `status`：草稿、已确认、已作废；结清及逾期按有效余额计算

应收与应付分别建对象。合同计划是约定，结算项是确认的收付义务，均不直接代表收入或成本。每笔义务只确认一次；后续发票通过发票行分配关联该项，不能再次生成相同应收应付。应收、应付本金为非负金额，草稿和作废项不进入结算；金额或到期日未确定的记录不能参与自动逾期计算。员工报销的债权人为申请员工对应自然人，金额依据已批报销明细，不能重复支付供应商与员工。内部交易双方各自的应收／应付分别保存。

### `receivable_adjustment` 与 `payable_adjustment`

应收项拥有的调整：`id`、`receivable_item_id` → `receivable_item.id`、`increase_receivable_amount`、`decrease_receivable_amount`、`currency`、`adjusted_at`、`adjustment_reason`、`source_document_id`、`approved_by_employee_id`、`approved_at`、`status`。

应付项拥有的调整：`id`、`payable_item_id` → `payable_item.id`、`increase_payable_amount`、`decrease_payable_amount`、`currency`、`adjusted_at`、`adjustment_reason`、`source_document_id`、`approved_by_employee_id`、`approved_at`、`status`。

每条调整只增加或减少一个方向，另一金额为零，金额均非负，增加或减少的一侧大于零。调整后本金为原金额加有效增加、减有效减少，不能小于零；币种沿用原项。已结算或已记账事项更正时保留原项及调整，不覆盖原始金额。调整导致超额结算时必须配套退款／核销撤销处理，不能把负余额静默归零。

### `payment`

- `id` 主键
- 付款方 ID 字段组：`payer_company_id`、`payer_legal_person_id`、`payer_person_id`，恰好一个
- 收款方 ID 字段组：`payee_company_id`、`payee_legal_person_id`、`payee_person_id`，恰好一个
- 被代付对象 ID 字段组：`payment_for_company_id`、`payment_for_legal_person_id`、`payment_for_person_id`，可选、最多一个
- `payment_number`：收付款记录编号
- `payment_kind`：普通收付、退款；款项方向由实际付款方和收款方表达
- `payment_purpose`：合同款、员工报销、预付款等
- `related_employee_id` 普通 ID 字段，可空 → `employee.id`，员工报销、工资等场景填写
- `refund_of_payment_id` 可空 → `payment.id`：退款对应的原付款
- `agency_basis_document_id` 可空 → `source_document.id`：代付或代收依据
- 被代收对象 ID 组：`receipt_for_company_id`、`receipt_for_legal_person_id`、`receipt_for_person_id`，可空且最多一个
- `payer_company_bank_account_id` / `payee_company_bank_account_id` 可空 → `company_bank_account.id`
- `confirmed_by_employee_id` / `confirmed_at` 可空：资金事实核对人和时间
- `paid_at` / `payment_amount` / `currency`：实际支付时间、实际收付本金及币种
- `bank_value_date` 可空：银行记账／起息日
- `bank_fee_amount` 可空：同币种银行手续费，单独保存，不直接冲减本金或应收应付
- `payment_channel`：支付渠道，如银行转账、现金、支付平台
- `payer_bank_account_number_snapshot` / `payer_bank_account_name_snapshot` 可空：实际付款银行账号及户名
- `payer_bank_name_snapshot` 可空：付款银行名称
- `payee_bank_account_number_snapshot` / `payee_bank_account_name_snapshot` 可空：实际收款银行账号及户名
- `payee_bank_name_snapshot` 可空：收款银行名称
- `bank_transaction_number` 可空：银行流水号
- `payment_platform_transaction_number` 可空：支付平台流水号
- `source_document_id` 普通 ID 字段，可空 → `source_document.id`
- `status`

实际付款方、实际收款方与被代付对象直接记录。普通员工报销付款的收款自然人须与 `related_employee_id` 对应自然人一致；退款时按实际反向收付记录；该字段不表示员工替公司承担成本。代付不改写合同买方、发票受票方或成本承担对象。

银行账号、户名及流水号使用字符串保存；快照记录实际支付时的信息。现金等不适用银行账号的渠道留空，不能把平台流水号填入银行账号字段。各渠道资料完整性由应用层处理，付款账户与收款账户分别记录。

### `payment_allocation`

付款拥有的核销明细：

- `id` 主键
- `payment_id` 普通 ID 字段 → `payment.id`
- `receivable_item_id` / `payable_item_id` → 对应结算项，恰好一个
- `settlement_amount` / `currency`：本次结算金额的正值及币种
- `settlement_action`：`apply` 核销、`reverse` 撤销核销
- `reverses_allocation_id` 可空 → `payment_allocation.id`：撤销时必填的原核销
- `allocated_at` / `allocated_by_employee_id` → 时间／`employee.id`
- `settlement_basis_document_id` 可空 → `source_document.id`
- `status`：草稿、已确认

收付款与应收／应付核销，发票经同一结算项追溯。支持分次、合并和无发票结算。普通核销按应收侧、应付侧分别检查，累计金额不超过该付款在该侧未分配的本金及目标项未结余额，币种一致。集团内部付款可能同时核销收款公司的应收和付款公司的应付，两侧各自限额，不能将两侧分配相加当作两笔资金流水。第三方代收／代付时核对实际资金方、原债务人／债权人和代理依据，不要求它们强行是同一对象。

仅已核对的有效实际付款参与核销；退款对应的收付方、银行账户主数据的所属方与快照差异由应用层核对。每个结算项有效已结算金额为已确认 `apply` 合计减 `reverse` 合计；未结余额为调整后本金减有效已结算金额。撤销关联的原记录必须是已确认的 `apply`，结算项、币种和应收／应付侧与原核销一致；同一原核销被撤销的累计金额不超过原核销额。误核销更正使用原付款 ID 记录 `reverse`，释放原付款额度后再正确核销，不修改已确认明细。

实际退款新建一笔反向收付记录，填写 `refund_of_payment_id`；退款核销使用退款付款 ID、`reverse` 和原核销 ID。退款核销金额在应收侧、应付侧分别不超过本次退款本金，应用层按原核销关系避免重复退回，所有关联该原付款的实际退款本金合计不得超过原付款本金。退款付款人与收款人按实际反向资金记录，差异需有代理依据。此类撤销不释放原付款的再次分配额度。若原付款尚未分配，退款可不建核销明细，但应从原付款未分配额度扣除。混合退回已分配及未分配款时，每侧未分配退款额为退款本金减该侧退款核销额，不能重复扣除。退款若伴随价款减免，另记应收／应付减少调整，不能仅凭资金退款推断业务取消。

未分配预收／预付款仍是一笔资金事实，可关联明确的预收／预付结算项；未确认业务归属前保留未分配状态。预收／预付与后续正式款项之间的抵扣、跨币种核销及净额结算另列待定，不在当前模型中静默抵消。

## 会计记录

### `ledger`

- `id` 主键
- `legal_person_id` 普通 ID 字段 → `legal_person.id`，明确账簿归属法人
- `scope_company_id` 普通 ID 字段，可空 → `company.id`，记录账簿范围对应的公司或分公司
- `ledger_code` / `name`
- `ledger_type` / `base_currency` / `status`

应用层检查账簿范围与归属法人一致：`scope_company_id` 对应公司时，使用该公司的 `legal_person_id`；对应分公司时，使用其总公司的 `legal_person_id`。归属按有效版本和账簿适用期间核对。分公司账簿保留分公司自身的范围 ID。非公司法人账簿可将 `scope_company_id` 留空，直接使用 `legal_person_id`；部门不替代账簿归属。账簿编号在法人及账簿范围内去重由应用层处理。

### `accounting_account`

账簿拥有的会计科目：`id`、`ledger_id` → `ledger.id`、`account_code`、`account_name`、`parent_account_id`（可空）→ `accounting_account.id`、`account_category`（资产、负债、权益、收入、费用等）、`normal_balance_direction`（借／贷）、`is_postable`、`valid_from`、`valid_to`、`status`。科目代码在账簿内由应用层去重；上级科目同账簿且层级无环，停用或非记账科目不能用于新分录。具体科目及核算政策不在逻辑模型中预置。

### `accounting_period`

账簿拥有的期间：`id`、`ledger_id` → `ledger.id`、`fiscal_year`、`period_number`、`period_start_date`、`period_end_date`、`period_status`（开放、关闭）、`closed_at`（可空）、`closed_by_employee_id`（可空）→ `employee.id`。期间边界和启闭规则由应用层执行，已关闭期间调整按选定会计流程处理。

### `journal_entry`

- `id` 主键
- `ledger_id` 普通 ID 字段 → `ledger.id`
- `accounting_period_id` → `accounting_period.id`
- `voucher_number` / `voucher_date`：凭证编号及业务记账日期
- `entry_description`：凭证摘要
- `prepared_by_employee_id` → `employee.id`
- `reviewed_by_employee_id` / `reviewed_at` 可空：审核人及时间
- `posted_by_employee_id` 可空 → `employee.id`
- `posted_at` / `status`：过账时间（未过账可空）及凭证生命周期
- `posting_batch_number` 可空：自动过账批次编号
- `posting_request_number` 可空：本次过账请求编号，自动过账时用于应用层幂等
- `posting_policy_code` / `posting_policy_version` 可空：自动过账所用政策及版本
- `adjustment_reason` 可空：手工调整或冲销原因
- `reversal_of_entry_id` 普通 ID 字段，可空 → `journal_entry.id`

核算法人由账簿明确，不再存一份可能与账簿冲突的核算身份。凭证号在账簿及期间内唯一，冲销凭证保留原凭证关联。

### `journal_line`

- `id` 主键
- `journal_entry_id` 普通 ID 字段 → `journal_entry.id`
- `line_number`
- `accounting_account_id` → `accounting_account.id`
- `account_code_snapshot` / `account_name_snapshot`：记账时科目代码及名称
- `line_description`：分录摘要
- `debit_amount` / `credit_amount` / `currency`
- 往来对象 ID 字段组：`counterparty_company_id`、`counterparty_legal_person_id`、`counterparty_person_id`，可选、最多一个
- `employee_id` / `department_id` 普通 ID 字段，可空 → `employee.id` / `department.id`
- `project_id` / `contract_stage_id` 普通 ID 字段，可空 → `project.id` / `contract_stage.id`
- `source_document_id` 普通 ID 字段，可空 → `source_document.id`

账簿记账金额使用账簿本位币。外币原币金额和汇率字段需在外币记账范围确定后补充，不直接把不同币种金额相加校验平衡。行号在凭证内唯一；借方和贷方不能同时为正，凭证按账簿规则平衡。

期间、科目和凭证须属于同一账簿，凭证日期落在选定期间且过账时期间开放。借贷金额非负，每行恰有一边为正；头部借贷合计由明细计算并核对，不单独人工填写。

### `journal_line_source`

分录拥有的业务来源明细：`id`、`journal_line_id` → `journal_line.id`；`invoice_line_id`、`payment_id`、`receivable_item_id`、`payable_item_id`、`receivable_adjustment_id`、`payable_adjustment_id`、`cost_allocation_id`、`cost_occurrence_id`、`revenue_recognition_id` 中恰好一个，均为普通 ID 字段并对应同名对象；`source_contribution_amount`、`currency`、`source_document_id`（可空）。记录本条来源对这条分录记账金额的贡献，合计与该分录非零一侧金额核对；多条来源允许汇总。

没有结构化业务来源的手工调整分录可直接记录 `journal_line.source_document_id` 与凭证调整原因，不伪造业务来源记录。结构化来源的贡献金额使用分录该侧记账金额的绝对值；负的收入冲回等由反向借贷表达。

同一来源可以用于不同科目分录，不把跨借贷行的来源贡献重复加总为业务金额。去重依据为业务来源、账簿、核算政策及该次过账意图，应用层处理幂等；错误更正通过冲销或调整凭证。

### `revenue_recognition`

项目合同的收入确认事实：`id`、`ledger_id` → `ledger.id`、`contract_id` → `contract.id`、`contract_term_version_id` → `contract_term_version.id`、`project_id`、`contract_stage_id`（可空）、`recognition_date`、`accounting_period_id`、`recognized_revenue_amount`、`currency`、`revenue_policy_code`、`revenue_policy_version`、`acceptance_event_id`（可空）、`source_document_id`、`approved_by_employee_id`、`approved_at`、`status`、`reversal_of_recognition_id`（可空）→ `revenue_recognition.id`。

保存本次确认金额及所采用政策的明确版本；币种与账簿本位币一致。冲回记录通过原记录 ID 和负的确认金额表达，应用层核对累计冲回不超过原确认额，并保持合同、账簿、项目和币种一致；不能将负确认额解释为负的借贷金额。分录通过 `journal_line_source.revenue_recognition_id` 追溯此事实，已审批不等于已过账；已确认和已入账分别查询。

收入确认、成本入账由核算政策与业务依据决定。验收、开票、付款不自动生成相同金额的收入或成本。当前支持记录已审批的确认结论及凭据，具体进度计量、履约义务分摊和自动收入计算策略仍需单独定义，不能把阶段验收金额直接作为收入。

## 应用层业务规则与场景检查

本节及各对象说明均为应用层规则，不生成数据库约束。以下金额仅为结构核对示例，不规定税率、收入确认或费用入账政策。

| 场景 | 字段路径与核对结果 |
|---|---|
| 分公司登记、部门和银行账户 | 分公司自己的 `company.id` 关联登记、部门和银行账户；`head_company_id` 只用于隶属，法人 ID 留空 |
| 同一公司向 A、B 供货 | 供货公司只建一份 `company`，A、B 各建自己的 `supplier` 档案，编号、准入及默认账期可以不同；各自合同与应付直接引用各自档案 |
| 供应商暂停后结清旧款 | 新采购按暂停状态控制；原合同、费用、应付及付款仍保留，存量付款不重新启用供应商也不改变原债权人 |
| 三阶段合同 | 含税总价 300,000 元，阶段含税价 60,000／120,000／120,000 元；开票计划和收付款计划各自存储，不能把三个金额口径混算 |
| 分批交付与复验 | 交付 10 项，首次通过 7、未通过 3，复验剩余 3 项通过；有效累计通过为 10，不是 13，条款换版也不重新累计 |
| 先开票、部分收款 | 应收项 60,000 元，发票分配关联同一应收项 60,000 元，付款核销 30,000 元；未结应收 30,000 元，开票不会再加一笔应收 |
| 未开票预收 | 确认合同约定预收项 30,000 元并到账 30,000 元，可先结算；后续开票关联原项，不重复确认义务 |
| 员工标准人工成本 | 8 小时 × 200 元／小时 = 1,600 元，保存已批工时、成本率版本、计算快照及成本事实；工资实际成本另有口径，不与标准成本重复相加 |
| 跨项目费用 | 一笔已确认可归集成本 10,000 元，项目 A 6,000 元、B 4,000 元；原支出及发票依据只记录一次 |
| 员工报销 | 员工垫付 500 元，已批报销 500 元，形成对应员工自然人的应付项；付款核销 500 元，成本不因报销及付款再增加 |
| 核销错误更正 | 原付款 30,000 元误核销后，以原付款记录撤销 30,000 元再核销正确应收项；有效占用仍为 30,000 元 |
| 部分退款 | 60,000 元应收已结 30,000 元，实际退回 10,000 元并撤销原核销对应部分；有效已结为 20,000 元、未结为 40,000 元；若还发生价款减免，另记减少调整 |
| 更名、税号或红字更正 | 原票名称、税号和金额保留；更正票关联原票，开票分配按票面符号记录，不自动改写应收、成本或收入 |
| A 签约、B 交付 | 合同卖方 A、交付公司 B、成本承担对象及雇佣对象按实际记录；若有内部交易则另建内部合同、应收应付和双方账簿 |
| 独立收入确认 | 发票 60,000 元、到账 30,000 元时，收入仍按收入确认记录及其政策依据查询；分录来源可以汇总多笔确认事实，不能从到账额推算 |

关联一致性：合同版本、品项、阶段、交付项、发票计划和结算项须属于相应合同；同时填写项目时须匹配项目合同集合。预算版本属于对应项目；员工、雇佣、任职、成本率的对象与期间一致。确认、审批及过账人员字段对应具体员工。

数量金额一致性：数量、正向成本和正向结算金额非负；分配按来源、口径、币种及有效状态控制上限。红字票、收入冲回保留明确负号语义；应收应付增减与核销撤销采用专用字段和明确动作。不能将核销、开票匹配、成本分配三种明细重复相加。

历史一致性：已引用的合同／登记版本、原票、原付款、原工时和原成本保留；新版本或更正记录明确关联原记录。失效来源不能继续新增分配；已有分配和账务通过相应更正流程处理。

### 汇总采用的有效记录

状态以普通字符串保存，应用层维护取值与流转；下表明确计算口径，不生成数据库枚举或检查约束。

| 对象 | 计入条件与更正方式 |
|---|---|
| 工时 | `record_status` 有效且 `approval_status` 通过；被替代记录不重复计入 |
| 成本事实及分配 | 成本 `status` 有效且审批通过，分配 `status` 有效；来源或分配替代后只计有效版本，标准人工及工资成本按选定口径分别统计 |
| 交付／验收 | 交付已提交且未撤回，验收 `status` 有效；按有效验收行累计通过量 |
| 发票及开票分配 | 已开具且未作废的票面记录，红字按实际符号计入；被红字更正的原票保留并与更正票一起计算净额 |
| 应收／应付及调整 | 已确认本金加已批准且有效的增加、减有效的减少；没有到期日不计入确定的逾期金额 |
| 付款及核销 | 实际付款 `status` 已确认，核销 `status` 已确认；按侧累计正向核销减撤销，实际退款另外核对资金净额 |
| 收入确认 | 已批准的确认事实及其有效冲回计算净确认额；已过账额另外从分录来源核对 |
| 会计分录 | 仅已过账凭证进入正式账务汇总；原凭证及反向冲销凭证共同计算净额 |

资料误录导致作废时，要先处理既有引用及金额占用并保留原因；实际发生后又退款或冲销，不能把原事实直接改为“未发生”。

## 本次整体复核补充清单（2026-10-11）

| 范围 | 补充的关键字段／明细 |
|---|---|
| 公司资料 | 联系电话、邮箱、办公地址、分公司负责人、注册资本及币种、营业期限、公司银行账户 |
| 供应商管理（后续补正） | 独立供应商档案、采购公司、实际供货对象、编号、准入、合作状态、采购负责人、默认结算条件、联系人、供货类别、资质与历史版本；合同／费用／应付直接关联 |
| 员工与组织 | 工作联系方式、实际入离职日期、用工合同期限、离职原因、部门负责人和联系方式 |
| 项目管理 | 项目类型与范围、实际启动／完成／结项日期、成员、计划工时、预算版本及审批 |
| 合同条款 | 合同名称、负责人、签署人、联系人、签约名称／税号快照、计价方式、含税／不含税金额、品项及版本 |
| 阶段与计划 | 阶段名称与负责人、交付项数量及单位、计划验收日期、分别建模的开票计划和收付款计划 |
| 工时与成本 | 工作说明、可计费标记、审批人、工资来源、成本口径版本、供应商、支出税额、员工垫付、发票匹配及报销 |
| 履约 | 交付编号、重提关联、交付项及数量、交付物版本、验收签署人、逐项通过／不通过数量 |
| 发票与结算 | 发票类型／代码／税率／票面合计／原票关联；独立应收应付、到期日、业务调整、退款和核销撤销 |
| 会计追溯 | 科目、期间、凭证日期、审核与过账人、政策版本、幂等请求编号、分录业务来源、独立收入确认事实 |

以上为逻辑模型及场景推演。后续已实现 Spring Boot 4、JPA、SQLite 与 Thymeleaf 的本地 Demo，包含物理 DDL、明确实体及主要流程的应用校验，具体见 [Demo 模型映射](demo-model-mapping.md) 和 [操作场景](demo-scenarios.md)。逻辑规则的完整政策范围仍以本文为准，不能据此宣称所有行业或税务场景已覆盖。

## 后续待定事项

1. 具体成本率来源、工资和标准成本的适用政策、审批权限及预算规则；当前已有字段记录所选口径与审批结论。
2. 收入确认的具体进度计量、履约义务分摊和自动计算规则；当前支持明确记录人工确认结论与来源。
3. 外币记账、跨币种核销、预收／预付抵扣、净额结算、坏账及汇兑处理；当前不进行隐式转换或抵消。
4. 内部交易自动对账和合并抵销，以及多级审批流程；基础合同、应收应付及分录可以分别记录。
5. 数据库产品、物理类型长度／精度、普通索引，以及应用层并发与期间控制的具体实现；继续遵循无外键、无触发器、无自定义数据库约束。
