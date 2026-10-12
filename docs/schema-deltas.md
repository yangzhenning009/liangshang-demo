# 相对 Schema 02 的偏差

本文只记录当前代码相对 `docs/02-企业领域逻辑Schema第一版.md` 的偏差。不改设计文档。通用审计字段 `created_at`、`updated_at`、`created_by` 按 Schema 的建模约定保留，不记为偏差。数据库 `NOT NULL` 只用于始终必填的基础字段，组内角色 ID 保持可空，这与 Schema 的数据库约定一致。

## 新增字段

### `expense_claim.claim_type`

| 项 | 内容 |
|---|---|
| 字段 | `claim_type`，`varchar(32)`，`NOT NULL` |
| 含义 | 报销单类别。Schema 的 `expense_claim` 只有单号、报销人、承担公司、币种和审批信息，没有类别。 |
| 允许值 | `项目费用`、`体检费用` |
| 原因 | 报销包要求至少区分项目费用和体检费用。类别放在报销单上，不改费用类型，也不改应付用途。 |

## 可空性与确认规则

### `payable_item.source_document_id`

| 项 | 内容 |
|---|---|
| 字段 | `source_document_id`，可空 `bigint` |
| Schema | 记为确认依据，未标可空。按数据库约定，列本身仍可空，确认时由应用层要求填写。 |
| 当前行为 | 员工报销批准生成应付时，有依据材料就抄过来，没有则留空。已确认的报销应付不强制有来源材料。 |
| 原因 | 报销单的依据材料是可空的。没有材料时仍要形成对员工本人的应付，不另造一份空材料。 |

## 人工成本快照

Schema 把 `labor_cost_rate_id`、`costed_hours`、`hourly_cost_amount_snapshot` 和 `cost_amount` 放在 `cost_occurrence`，不放在 `work_log`。当前实体按这个位置保存。

| 项 | 内容 |
|---|---|
| 写入时机 | 工时批准时生成一笔 `cost_occurrence`，抄入当时的成本率主键、单价、计入小时和金额。 |
| 之后 | 修改原成本率或另记版本，不回写这笔成本。 |
| 旧文件 | 更早的库可能在 `work_log` 上留有 `labor_cost_rate_id`、`hourly_cost_amount_snapshot`、`labor_cost_amount`、`labor_cost_currency`。这些列不在 Schema 的 `work_log` 里，当前实体也不映射它们。启动时若发现这些列，把已通过工时抄到 `cost_occurrence`。升级后应删除 `data/liangshan.db`，不要依赖这次抄写。 |

## 规则比 Schema 更严

### 费用分配合计

| 项 | 内容 |
|---|---|
| Schema | 有效分配合计不得超过来源可分配金额。 |
| 当前 | 只要存在有效分配，合计必须等于 `cost_amount`。没有分配行时允许保存。 |
| 原因 | 跨项目费用场景要求 6,000 与 4,000 正好分完 10,000，少分或多分都拒绝。 |

## 取值由应用层收窄

Schema 给出字段、未穷尽取值的，当前页面只用下列字符串。数据库仍是普通字符串。

| 字段 | 允许值 |
|---|---|
| `cost_occurrence.cost_type` | `云服务`、`人工成本`、`体检`、`差旅`、`其他` |
| `cost_occurrence.cost_basis` | `实际费用`、`标准人工成本`、`工资成本` |
| `expense_claim.approval_status` | `草稿`、`待审`、`通过`、`驳回` |

## 发票与收付

`payment` 已补上 Schema 中的被代付、被代收、代收付依据、银行账户、手续费、账户快照和流水号，仍是同一张表。

`invoice`、`invoice_line`、`invoice_line_allocation`、`receivable_item`、`receivable_adjustment`、`payable_adjustment` 按 Schema 建表。关联 ID 保持可空，不建外键。

`cost_invoice_line` 的匹配金额按发票行含税金额封顶，符号须与票面一致，币种须与发票一致。

确认应收时，页面上的开票计划只用来推算到期日，不另加 `receivable_item.billing_schedule_id`。Schema 的应收也没有这一列。相对触发的实际验收或发票，以及算出的到期日，写在应收或应付上。

发票可以覆盖公司或法人债权人的结算项。开票方不能是自然人，因此债权人是自然人的应付（例如员工报销）不能从发票行关联。

## 已知缺口

### 发票开票方只能是公司或法人

| 项 | 内容 |
|---|---|
| 现状 | `invoice` 的开票方在 `issuer_company_id` 与 `issuer_legal_person_id` 中恰好一个，没有自然人开票方。 |
| 后果 | 债权人是自然人的应付，包括员工报销生成的应付，不能用发票行去关联。 |
| 依据 | Schema 的开票方同样是公司或法人。这里记为当前不能覆盖的业务缺口，不另加字段。 |

### 应收不保存产生到期日的开票计划

| 项 | 内容 |
|---|---|
| 现状 | 确认应收时，表单上的开票计划只用来推算到期日。`receivable_item` 不保存 `billing_schedule_id`。 |
| 落库 | 算出的 `due_date`，以及触发用的验收或发票，写在应收上。 |
| 依据 | Schema 的应收没有开票计划列。再次打开表单时看不到当时选的是哪一条开票计划。 |

## 会计状态与过账

Schema 给出会计字段，未穷尽状态字符串。当前页面只用下列取值。数据库仍是普通字符串，没有检查约束。

| 字段 | 允许值 |
|---|---|
| `ledger.ledger_type` | `公司账簿`、`法人账簿` |
| `ledger.status` / `accounting_account.status` | `启用`、`停用` |
| `accounting_account.account_category` | `资产`、`负债`、`权益`、`收入`、`费用` |
| `accounting_account.normal_balance_direction` | `借`、`贷` |
| `accounting_period.period_status` | `开放`、`关闭` |
| `journal_entry.status` | `草稿`、`已过账` |
| `revenue_recognition.status` | `草稿`、`已审批`、`已作废` |

| 项 | 内容 |
|---|---|
| 草稿平衡 | Schema 要求凭证按账簿规则平衡。当前草稿可以借贷不平衡，过账时才要求借方合计等于贷方合计且大于 0。 |
| 期间重开 | 已关闭期间可以重新开放。重开不删除已过账凭证；关闭期间内不能新过账，也不能做冲销。 |
| 月度核算 | 不落表。按所选月份汇总已审批收入、有效成本、已确认应收和已确认应付，不是科目余额。集团合计等于总部本级加上各分公司。 |
