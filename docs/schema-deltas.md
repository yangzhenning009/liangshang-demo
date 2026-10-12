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

## 尚未按 Schema 建全的列

`payment` 目前只保存收付款双方、单号、种类、用途、关联员工、退款原单、支付时间、金额、币种、渠道、核对人、银行起息日、来源材料和状态。下列 Schema 字段还没有列，后续资金包补上，不另建第二张付款表：

- 被代付对象：`payment_for_company_id`、`payment_for_legal_person_id`、`payment_for_person_id`
- 被代收对象：`receipt_for_company_id`、`receipt_for_legal_person_id`、`receipt_for_person_id`
- `agency_basis_document_id`
- `payer_company_bank_account_id`、`payee_company_bank_account_id`
- `bank_fee_amount`
- 付款账户快照：`payer_bank_account_number_snapshot`、`payer_bank_account_name_snapshot`、`payer_bank_name_snapshot`
- 收款账户快照：`payee_bank_account_number_snapshot`、`payee_bank_account_name_snapshot`、`payee_bank_name_snapshot`
- `bank_transaction_number`、`payment_platform_transaction_number`

`invoice`、`invoice_line`、`invoice_line_allocation`、`receivable_item`、`receivable_adjustment`、`payable_adjustment` 尚未建表。`cost_invoice_line` 已能保存发票行号，但还没有发票行对象，因此还不能按票面含税金额检查匹配上限。
