<template>
  <div class="category-branch" :class="[`level-${level}`]">
    <div v-for="item in nodes" :key="item.categoryId" class="category-node">
      <div class="category-node__main">
        <div class="category-node__content">
          <div class="category-node__title-row">
            <div
              class="category-node__title-group"
              :class="{ 'is-clickable': item.children?.length }"
              @click="item.children?.length && toggleNode(item.categoryId)"
            >
              <div
                v-if="item.children?.length"
                class="category-node__toggle"
              >
                <el-icon class="category-node__toggle-icon" :class="{ 'is-expanded': isExpanded(item.categoryId) }">
                  <ArrowRight />
                </el-icon>
                <span>{{ isExpanded(item.categoryId) ? '收起' : '展开' }}</span>
              </div>
              <h3 class="category-node__name">{{ item.categoryName }}</h3>
            </div>
            <div class="category-node__badges">
              <el-tag size="small" effect="plain">L{{ level + 1 }}</el-tag>
              <el-tag :type="item.status === '0' ? 'success' : 'info'" size="small">
                {{ item.status === '0' ? '启用' : '停用' }}
              </el-tag>
              <el-tag v-if="item.children?.length" type="warning" size="small" effect="plain">
                {{ item.children.length }} 个子类
              </el-tag>
            </div>
          </div>
          <div class="category-node__code">编码: {{ item.categoryCode || '--' }}</div>
          <div class="category-node__meta">
            <span>排序 {{ item.orderNum ?? 0 }}</span>
            <span>{{ formatDate(item.createTime) }}</span>
          </div>
        </div>
        <div class="category-node__actions">
          <el-button link type="primary" icon="Plus" @click="$emit('add', item)">新增子类</el-button>
          <el-button link type="primary" icon="Edit" @click="$emit('edit', item)">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="$emit('delete', item)">删除</el-button>
        </div>
      </div>

      <CategoryBranch
        v-if="item.children?.length && isExpanded(item.categoryId)"
        :nodes="item.children"
        :level="level + 1"
        @add="$emit('add', $event)"
        @edit="$emit('edit', $event)"
        @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup name="CategoryBranch">
import { ArrowRight } from '@element-plus/icons-vue'

defineProps({
  nodes: {
    type: Array,
    default: () => []
  },
  level: {
    type: Number,
    default: 0
  }
})

defineEmits(['add', 'edit', 'delete'])

const { proxy } = getCurrentInstance()
const expandedMap = reactive({})

function formatDate(value) {
  if (!value) {
    return '--'
  }
  return proxy?.parseTime ? proxy.parseTime(value, '{y}-{m}-{d} {h}:{i}:{s}') : value
}

function isExpanded(categoryId) {
  return expandedMap[categoryId] !== false
}

function toggleNode(categoryId) {
  expandedMap[categoryId] = !isExpanded(categoryId)
}
</script>

<style scoped lang="scss">
.category-branch {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;

  &.level-0 {
    gap: 18px;
  }
}

.level-1,
.level-2,
.level-3,
.level-4,
.level-5 {
  grid-template-columns: minmax(0, 1fr);
}

.category-node {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
}

.category-node__main {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  transition: all 0.3s ease;

  &:hover {
    border-color: #cbd5e1;
    box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
    transform: translateY(-2px);
  }
}

.category-node__content {
  flex: 1;
  min-width: 0;
}

.category-node__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.category-node__title-group {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;

  &.is-clickable {
    cursor: pointer;
  }
}

.category-node__toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: 0;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  transition: background 0.2s ease;

  .category-node__title-group.is-clickable:hover & {
    background: #dbeafe;
  }
}

.category-node__toggle-icon {
  transition: transform 0.2s ease;

  &.is-expanded {
    transform: rotate(90deg);
  }
}

.category-node__name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.category-node__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-node__code {
  margin-bottom: 12px;
  font-size: 13px;
  color: #6b7280;
}

.category-node__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
  font-size: 12px;
  color: #94a3b8;
}

.category-node__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.level-1,
.level-2,
.level-3,
.level-4,
.level-5 {
  margin-left: 24px;
  padding-left: 18px;
  border-left: 1px dashed #dbe3ee;
}

.level-1 .category-node__main,
.level-2 .category-node__main,
.level-3 .category-node__main,
.level-4 .category-node__main,
.level-5 .category-node__main {
  padding: 16px 18px;
  background: #ffffff;
}

@media (max-width: 768px) {
  .category-branch {
    grid-template-columns: minmax(0, 1fr);
  }

  .category-node__main,
  .category-node__title-row,
  .category-node__actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .category-node__title-group {
    flex-wrap: wrap;
  }

  .level-1,
  .level-2,
  .level-3,
  .level-4,
  .level-5 {
    margin-left: 14px;
    padding-left: 12px;
  }
}
</style>
