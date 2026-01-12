# 甘特图数据结构检查

## 预期的 JSON 结构

```json
{
  "data": [
    {
      "id": 2,
      "text": "智慧景区系统",
      "type": "project",
      "start_date": "2025-01-01",
      "end_date": "2025-12-31",
      "duration": 365,
      "progress": 0,
      "parent": null,
      "subtasks": [
        {
          "id": 10,
          "text": "Sprint 1",
          "type": "iteration",
          "start_date": "2025-01-01",
          "end_date": "2025-01-14",
          "duration": 14,
          "progress": 0,
          "parent": 2,
          "subtasks": [
            {
              "id": 20,
              "text": "需求分析",
              "type": "task",
              "start_date": "2025-01-01",
              "end_date": "2025-01-07",
              "duration": 7,
              "progress": 0,
              "parent": 10,
              "subtasks": []
            }
          ]
        }
      ]
    }
  ],
  "links": []
}
```

## 前端扁平化后的结构

```javascript
[
  { id: 2, text: "智慧景区系统", type: "project", parent: 0 },
  { id: 10, text: "Sprint 1", type: "iteration", parent: 2 },
  { id: 20, text: "需求分析", type: "task", parent: 10 }
]
```

## 检查要点

1. ✅ 每个 node 有唯一的 id
2. ✅ parent 指向父节点的 id
3. ✅ 根节点的 parent 为 null（前端会转为 0）
4. ✅ subtasks 数组包含子节点
5. ✅ 不会有循环引用（如 A.parent = B, B.parent = A）
