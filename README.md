# Approval Workflow Challenge

![Workflow Diagram](https://user-images.githubusercontent.com/112865589/191920630-6c4e8f8e-a8d9-42c2-b31e-ab2c881ed297.jpg)

> **Fig. 1** Workflow added as per requirements  
> **Company ID for above ^:** `00000000-0000-0000-0000-000000000001`

---

## Overview

This project implements a configurable **invoice approval workflow engine**.  
Each company defines a workflow consisting of ordered rules. When an invoice is created, it is evaluated against the active workflow, and approval requests are generated accordingly.

---

## Assumptions

While designing and implementing the solution, the following assumptions were made:

1. Each company can define **only one active workflow**. Every new invoice will go through that workflow.
2. A company can modify its workflow at any time, even while the **current** version is actively processing invoices.
3. All amounts are expressed in **USD**.

---

## Additional Assumptions

1. The system is operated via a **CLI**. In a production environment, this would ideally be exposed via **API endpoints**. 
If we had APIs and an interface, we can control and provide options/configs to the UI for the company to edit.
2. Invoice creation via CLI mimics an Invoice Received event.  
   In production, this would be triggered by a real event and act as the workflow trigger.
3. No external workflow engine is used.  
   In a production system, existing solutions for workflow engines could be considered - **eg Temporal, Flowable, Cadence, AWS Step Functions**.
4. Notifications are sent **asynchronously** to decouple them from the `ApprovalRequest` lifecycle.  
   In a real application:
    - A client would receive and deliver the notification.
    - The client would return a response.
    - Batch processing or scheduled jobs could be used to notify approvers for multiple pending invoices.
5. Processing the same invoice ID multiple times does **not** resend notifications.  
   The processor is **idempotent**.
6. Updating a workflow:
    - Creates a new workflow version.
    - Sets the previous version to **inactive**.
    - Requires submitting the **entire workflow configuration** (hence full JSON input in CLI).
    - Previously processed invoices remain linked to the workflow version that processed them.
    - If a workflow is updated while an invoice is being processed, processing completes using the workflow version it started with (no read/write locks are applied).
7. Rule Conditions only evaluate invoice attributes and static configuration, they are "pure" without side effects.
8. Approval Request are open to supporting a lifecycle. This model allows us to implement new features easily - approval process, batch processing (maybe for new notifications).
9. Failures in notification delivery do not fail workflow execution.
10. The CLI is a small orchestration layer, not a business layer. All the business logic lives in application services that can later be reused by APIs, workers, or event consumers.
11. Future improvement for scaling is caching the workflow (rules and actions for the company). This is easy, since we have decoupled it. 
For example, other engines, like in AWS Step Functions - definitions are stored in DB, loaded into memory and cached. And in Temporal, the workflow definitions are versioned and cached in workers.
12. We should introduce an index on companyId and status active for the workflow - I didn't add it, since I am using in memory h2 db.

---

## Database Schema / Model

![Database Schema](src/main/resources/db-diagram.png)

### Rule Evaluation Order

`stepOrder` determines rule execution order.  
Rules with a lower `stepOrder` are evaluated first.

Example:
>Rule A: stepOrder = 10
>Rule B: stepOrder = 20
>Rule C: stepOrder = 30


The engine evaluates rules sequentially:
>Rule A → Rule B → Rule C

If more than 1 rule results in an action, all actions will be performed.

This enables:

- Conditional flows
- Decision-based branching
- Structured progression through workflow logic

The schema also supports workflows where multiple rules can trigger simultaneously if conditions are not mutually exclusive.

> **Current limitation:** 
> The application currently supports only sending approval notifications (`CREATE_APPROVAL_REQUEST`).

---

## Running the Application

```bash
mvn clean package
java -jar target/invoice-approval-1.0.0.jar
```

Creating a Workflow (When adding a new workflow for the company, the old one is updated to inactive). 
A company only has 1 active workflow at a time.

| Field                     | Type       | Supported Operators            |
| ------------------------- | ---------- | ------------------------------ |
| `amount`                  | BigDecimal | Numeric operators              |
| `department`              | String     | EQ, NEQ                        |
| `requiresManagerApproval` | Boolean    | EQ, NEQ (`"true"` / `"false"`) |


Supported Operations

| Operator | Meaning |
| -------- | ------- |
| `EQ`     | == 0    |
| `NEQ`    | != 0    |
| `GT`     | > 0     |
| `GTE`    | >= 0    |
| `LT`     | < 0     |
| `LTE`    | <= 0    |

Example Workflow JSON

Use the following JSON to create a new workflow (Action [2] in CLI). 
You can change the company id and it will tell you such company doesn't exist.
The json bellow has some tiny changes that you can test.

Important:
Edit the JSON before pasting it into the CLI.
The Scanner reads only the initial pasted content and ignores subsequent edits.

```json
{
  "companyId": "00000000-0000-0000-0000-000000000001",
  "name": "Light Invoice Approval Workflow v2",
  "rules": [
    {
      "name": "A1 — Small invoice to finance team",
      "stepOrder": 10,
      "conditions": [
        { "field": "amount", "operator": "LTE", "value": "100.00" }
      ],
      "actions": [
        { "actionType": "CREATE_APPROVAL_REQUEST", "approverId": "00000000-0000-0000-0000-000000000011" }
      ]
    },
    {
      "name": "A3 — Mid invoice, manager approval required",
      "stepOrder": 30,
      "conditions": [
        { "field": "amount", "operator": "GT", "value": "50.00" },
        { "field": "amount", "operator": "LTE", "value": "100.00" },
        { "field": "requiresManagerApproval", "operator": "EQ", "value": "true" }
      ],
      "actions": [
        { "actionType": "CREATE_APPROVAL_REQUEST", "approverId": "00000000-0000-0000-0000-000000000012" }
      ]
    },
    {
      "name": "B1 — Large non-marketing invoice to CFO",
      "stepOrder": 40,
      "conditions": [
        { "field": "amount", "operator": "GT", "value": "100.00" },
        { "field": "department", "operator": "NEQ", "value": "Marketing" }
      ],
      "actions": [
        { "actionType": "CREATE_APPROVAL_REQUEST", "approverId": "00000000-0000-0000-0000-000000000013" }
      ]
    }
  ]
}
```
