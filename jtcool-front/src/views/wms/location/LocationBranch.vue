<template>
  <div class="location-branch" :class="[`level-${level}`]">
    <div v-for="item in nodes" :key="item.id" class="location-node">
      <div class="location-node__main">
        <div class="location-node__content">
          <div class="location-node__title-row">
            <div
              class="location-node__title-group"
              :class="{ 'is-clickable': item.children?.length || canShowInventory(item) }"
              @click="toggleNode(item.id)"
            >
              <div
                v-if="item.children?.length || canShowInventory(item)"
                class="location-node__toggle"
              >
                <el-icon class="location-node__toggle-icon" :class="{ 'is-expanded': isExpanded(item.id) }">
                  <ArrowRight />
                </el-icon>
                <span>{{ isExpanded(item.id) ? '收起' : '展开' }}</span>
              </div>
              <div class="location-node__icon" :class="`location-node__icon--${item.type}`">
                <el-icon><component :is="getTypeIcon(item.type)" /></el-icon>
              </div>
              <h3 class="location-node__name">{{ item.name }}</h3>
            </div>
            <div class="location-node__badges">
              <el-tag size="small" effect="plain">{{ getTypeLabel(item.type) }}</el-tag>
              <el-tag :type="item.status === '0' ? 'success' : 'info'" size="small">
                {{ item.status === '0' ? '启用' : '停用' }}
              </el-tag>
              <el-tag v-if="item.children?.length" type="warning" size="small" effect="plain">
                {{ item.children.length }} 个子节点
              </el-tag>
            </div>
          </div>
          <div class="location-node__code">编码: {{ item.code || '--' }}</div>
          <div class="location-node__meta">
            <span>层级 L{{ level + 1 }}</span>
            <span v-if="item.parentName">父级: {{ item.parentName }}</span>
          </div>
        </div>
        <div class="location-node__actions">
          <el-button link type="primary" icon="Plus" @click="$emit('add', item)" v-if="item.type !== 'shelf'">新增子节点</el-button>
          <el-button link type="primary" icon="Edit" @click="$emit('edit', item)">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="$emit('delete', item)">删除</el-button>
        </div>
      </div>

      <div v-if="isExpanded(item.id)" class="location-node__expanded">
        <div v-if="canShowInventory(item)" class="inventory-detail" v-loading="inventoryLoading[item.id]">
          <div class="inventory-detail__header">
            <el-icon><Box /></el-icon>
            <span>库存明细</span>
          </div>
          <div v-if="inventoryData[item.id]?.length" class="inventory-list">
            <div v-for="inv in inventoryData[item.id]" :key="inv.productId" class="inventory-item">
              <div class="inventory-item__info">
                <div class="inventory-item__name">{{ inv.productName }}</div>
                <div class="inventory-item__stats">
                  <span class="stat">总量: <strong>{{ inv.quantity }}</strong></span>
                  <span class="stat">可用: <strong :class="{'text-warning': inv.availableQuantity < inv.warningStock}">{{ inv.availableQuantity }}</strong></span>
                  <span class="stat stat--locked">锁定: {{ inv.lockedQuantity }}</span>
                </div>
              </div>
              <div class="inventory-item__logs">
                <el-button link size="small" @click="showLogs(item, inv)">
                  <el-icon><TrendCharts /></el-icon>
                  出入库记录
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无库存" :image-size="60" />
        </div>

        <LocationBranch
          v-if="item.children?.length"
          :nodes="item.children"
          :level="level + 1"
          @add="$emit('add', $event)"
          @edit="$emit('edit', $event)"
          @delete="$emit('delete', $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup name="LocationBranch">
import { ArrowRight, Box, Grid, Location, OfficeBuilding, TrendCharts } from '@element-plus/icons-vue'
import { listInventory } from '@/api/wms/inventory'
import { listInventoryLog } from '@/api/wms/inventoryLog'
import { parseTime } from '@/utils/jtcool'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
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

const expandedMap = reactive({})
const inventoryData = reactive({})
const inventoryLoading = reactive({})

function isExpanded(id) {
  return expandedMap[id] === true
}

function canShowInventory(item) {
  return item.type === 'location' || item.type === 'shelf'
}

async function toggleNode(id) {
  const wasExpanded = isExpanded(id)
  expandedMap[id] = !wasExpanded

  if (!wasExpanded) {
    const node = findNodeById(props.nodes, id)
    if (node && canShowInventory(node)) {
      await loadInventory(node)
    }
  }
}

function findNodeById(nodes, id) {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

async function loadInventory(item) {
  inventoryLoading[item.id] = true
  try {
    const params = {}
    if (item.type === 'location') params.locationId = item.id
    if (item.type === 'shelf') params.shelfId = item.id

    const res = await listInventory(params)
    inventoryData[item.id] = res.rows || []
  } catch (error) {
    ElMessage.error('加载库存数据失败')
    inventoryData[item.id] = []
  } finally {
    inventoryLoading[item.id] = false
  }
}

async function showLogs(location, inventory) {
  try {
    const res = await listInventoryLog({
      productId: inventory.productId,
      locationId: location.type === 'location' ? location.id : undefined,
      shelfId: location.type === 'shelf' ? location.id : undefined,
      pageNum: 1,
      pageSize: 10
    })

    const logs = res.rows || []
    const logHtml = logs.length
      ? logs.map(log => `
          <div style="padding: 8px; border-bottom: 1px solid #eee;">
            <strong>${log.operationType === 'in' ? '入库' : '出库'}</strong>
            数量: ${log.quantity} |
            时间: ${parseTime(log.createTime, '{y}-{m}-{d} {h}:{i}:{s}')}
          </div>
        `).join('')
      : '<p style="text-align: center; color: #999;">暂无记录</p>'

    ElMessageBox.alert(logHtml, `${inventory.productName} - 出入库记录`, {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    })
  } catch (error) {
    ElMessage.error('加载出入库记录失败')
  }
}

function getTypeLabel(type) {
  const labels = {
    warehouse: '仓库',
    area: '库区',
    location: '库位',
    shelf: '货架'
  }
  return labels[type] || '节点'
}

function getTypeIcon(type) {
  const icons = {
    warehouse: OfficeBuilding,
    area: Grid,
    location: Location,
    shelf: Box
  }
  return icons[type] || Box
}
</script>

<style scoped lang="scss">
.location-branch {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;

  &.level-0 {
    gap: 18px;
  }
}

.level-1,
.level-2,
.level-3 {
  grid-template-columns: minmax(0, 1fr);
}

.location-node {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
}

.location-node__expanded {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.location-node__main {
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

.location-node__content {
  flex: 1;
  min-width: 0;
}

.location-node__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.location-node__title-group {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;

  &.is-clickable {
    cursor: pointer;
  }
}

.location-node__toggle {
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

  .location-node__title-group.is-clickable:hover & {
    background: #dbeafe;
  }
}

.location-node__toggle-icon {
  transition: transform 0.2s ease;

  &.is-expanded {
    transform: rotate(90deg);
  }
}

.location-node__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  color: #fff;
  font-size: 16px;
  flex-shrink: 0;
}

.location-node__icon--warehouse {
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 100%);
}

.location-node__icon--area {
  background: linear-gradient(135deg, #16a34a 0%, #4ade80 100%);
}

.location-node__icon--location {
  background: linear-gradient(135deg, #ea580c 0%, #fb923c 100%);
}

.location-node__icon--shelf {
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
}

.location-node__name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.location-node__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.location-node__code {
  margin-bottom: 12px;
  font-size: 13px;
  color: #6b7280;
}

.location-node__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
  font-size: 12px;
  color: #94a3b8;
}

.location-node__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.level-1,
.level-2,
.level-3 {
  margin-left: 24px;
  padding-left: 18px;
  border-left: 1px dashed #dbe3ee;
}

.level-1 .location-node__main,
.level-2 .location-node__main,
.level-3 .location-node__main {
  padding: 16px 18px;
  background: #ffffff;
}

@media (max-width: 768px) {
  .location-branch {
    grid-template-columns: minmax(0, 1fr);
  }

  .location-node__main,
  .location-node__title-row,
  .location-node__actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .location-node__title-group {
    flex-wrap: wrap;
  }

  .level-1,
  .level-2,
  .level-3 {
    margin-left: 14px;
    padding-left: 12px;
  }
}

.inventory-detail {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fafbfc;
}

.inventory-detail__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.inventory-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.inventory-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;

  &:hover {
    border-color: #cbd5e1;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.inventory-item__info {
  flex: 1;
}

.inventory-item__name {
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.inventory-item__stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #6b7280;

  .stat {
    strong {
      color: #111827;
      font-weight: 600;
    }
  }

  .stat--locked {
    color: #9ca3af;
  }
}

.text-warning {
  color: #f59e0b !important;
}

.inventory-item__logs {
  flex-shrink: 0;
}
</style>
