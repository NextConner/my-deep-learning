<template>
  <div class="app-container workbench">
    <div class="hero-panel">
      <div>
        <p class="hero-panel__eyebrow">个人工作台</p>
        <h2 class="hero-panel__title">今日待办与订单概览</h2>
        <p class="hero-panel__desc">主页面控制在一屏内，详细清单通过弹窗展开查看。</p>
      </div>
      <div class="hero-panel__meta">
        <div class="hero-panel__meta-item">
          <span class="label">待处理总量</span>
          <strong>{{ totalPending }}</strong>
        </div>
        <div class="hero-panel__meta-item">
          <span class="label">近7日订单</span>
          <strong>{{ orderMetrics.sevenDayOrders }}</strong>
        </div>
      </div>
    </div>

    <el-row :gutter="14" class="summary-row">
      <el-col
        v-for="card in summaryCards"
        :key="card.key"
        :xs="12"
        :sm="12"
        :lg="6"
      >
        <el-card shadow="hover" class="summary-card" :class="`summary-card--${card.key}`">
          <div class="summary-card__head">
            <div class="summary-card__icon">
              <component :is="card.icon" />
            </div>
            <el-tag size="small" :type="card.tagType">{{ card.tagText }}</el-tag>
          </div>
          <div class="summary-card__value">{{ card.value }}</div>
          <div class="summary-card__title">{{ card.title }}</div>
        </el-card>
      </el-col>
    </el-row>

    <div class="content-grid">
      <el-card shadow="never" class="panel-card compact-card">
        <template #header>
          <div class="panel-card__header">
            <span>待处理订单</span>
            <el-button link type="primary" @click="dialogs.orders = true">查看全部</el-button>
          </div>
        </template>
        <div class="mini-list">
          <div v-for="item in pendingOrdersPreview" :key="item.orderNo" class="mini-list__item">
            <div>
              <div class="mini-list__title">{{ item.orderNo }}</div>
              <div class="mini-list__meta">{{ item.customerName }} | {{ parseTime(item.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</div>
            </div>
            <div class="mini-list__side">
              <el-tag :type="item.priorityType" effect="light">{{ item.priority }}</el-tag>
              <strong>{{ item.amount }}</strong>
            </div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="panel-card compact-card">
        <template #header>
          <div class="panel-card__header">
            <span>待处理审批</span>
            <el-button link type="primary" @click="dialogs.approvals = true">查看全部</el-button>
          </div>
        </template>
        <div class="mini-list">
          <div v-for="item in pendingApprovalsPreview" :key="item.id" class="mini-list__item">
            <div>
              <div class="mini-list__title">{{ item.title }}</div>
              <div class="mini-list__meta">{{ item.applicant }} | {{ item.submitTime }}</div>
            </div>
            <div class="mini-list__side">
              <el-tag :type="item.levelType" effect="dark">{{ item.level }}</el-tag>
              <strong>{{ item.amount }}</strong>
            </div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="panel-card compact-card">
        <template #header>
          <div class="panel-card__header">
            <span>出入库待办</span>
            <el-button link type="primary" @click="dialogs.inventory = true">查看全部</el-button>
          </div>
        </template>
        <div class="mini-list">
          <div v-for="item in inventoryPreview" :key="item.docNo" class="mini-list__item">
            <div>
              <div class="mini-list__title">{{ item.docNo }}</div>
              <div class="mini-list__meta">{{ item.bizType }} | {{ item.warehouseName }}</div>
            </div>
            <div class="mini-list__side">
              <el-tag :type="item.statusType">{{ item.status }}</el-tag>
              <strong>{{ item.owner }}</strong>
            </div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="panel-card metrics-card">
        <template #header>
          <div class="panel-card__header">
            <span>订单统计</span>
            <el-button link type="primary" @click="metricsExpanded = !metricsExpanded">
              {{ metricsExpanded ? '收起明细' : '展开明细' }}
            </el-button>
          </div>
        </template>
        <div class="metrics-overview">
          <div class="metric-chip">
            <span>昨日订单</span>
            <strong>{{ orderMetrics.yesterdayOrders }}</strong>
          </div>
          <div class="metric-chip">
            <span>昨日成交</span>
            <strong>{{ orderMetrics.yesterdaySales }}</strong>
          </div>
          <div class="metric-chip">
            <span>7日订单</span>
            <strong>{{ orderMetrics.sevenDayOrders }}</strong>
          </div>
          <div class="metric-chip">
            <span>完成率</span>
            <strong>{{ orderMetrics.fulfillmentRate }}</strong>
          </div>
        </div>
        <el-collapse-transition>
          <div v-show="metricsExpanded" class="metric-grid">
            <div class="metric-grid__item">
              <span>昨日客单价</span>
              <strong>{{ orderMetrics.yesterdayAvg }}</strong>
            </div>
            <div class="metric-grid__item">
              <span>昨日取消单</span>
              <strong>{{ orderMetrics.yesterdayCancelled }}</strong>
            </div>
            <div class="metric-grid__item">
              <span>7日成交额</span>
              <strong>{{ orderMetrics.sevenDaySales }}</strong>
            </div>
            <div class="metric-grid__item">
              <span>日均订单</span>
              <strong>{{ orderMetrics.sevenDayAverage }}</strong>
            </div>
          </div>
        </el-collapse-transition>
      </el-card>
    </div>

    <el-dialog v-model="dialogs.orders" title="待处理订单" width="980px">
      <el-table :data="pendingOrders" stripe>
        <el-table-column label="订单编号" prop="orderNo" min-width="140" />
        <el-table-column label="客户名称" prop="customerName" min-width="140" />
        <el-table-column label="订单类型" prop="orderType" min-width="110" />
        <el-table-column label="金额" prop="amount" min-width="100" />
        <el-table-column label="优先级" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.priorityType" effect="light">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" min-width="180">
          <template #default="{ row }">
            {{ parseTime(row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="dialogs.approvals" title="待处理审批" width="780px">
      <el-table :data="pendingApprovals" stripe>
        <el-table-column label="审批标题" prop="title" min-width="180" />
        <el-table-column label="申请人" prop="applicant" min-width="120" />
        <el-table-column label="提交时间" prop="submitTime" min-width="120" />
        <el-table-column label="级别" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.levelType">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" prop="amount" min-width="120" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="dialogs.inventory" title="待处理出入库单" width="1100px">
      <el-table :data="inventoryTodos" stripe>
        <el-table-column label="单据编号" prop="docNo" min-width="150" />
        <el-table-column label="业务类型" prop="bizType" min-width="120" />
        <el-table-column label="仓库" prop="warehouseName" min-width="140" />
        <el-table-column label="关联单号" prop="sourceNo" min-width="150" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.statusType">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ parseTime(row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
          </template>
        </el-table-column>
        <el-table-column label="负责人" prop="owner" min-width="100" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="Index">
import { reactive, ref, computed } from 'vue'
import { Box, DocumentChecked, Histogram, ShoppingCart } from '@element-plus/icons-vue'
import { parseTime } from '@/utils/jtcool'

const dialogs = reactive({
  orders: false,
  approvals: false,
  inventory: false
})

const metricsExpanded = ref(false)

const pendingOrders = ref([
  {
    orderNo: 'SO20260317001',
    customerName: '华东工业设备有限公司',
    orderType: '销售订单',
    amount: '¥18,600',
    priority: '高',
    priorityType: 'danger',
    createTime: '2026-03-17 09:10'
  },
  {
    orderNo: 'SO20260317002',
    customerName: '星河电子科技',
    orderType: '补货订单',
    amount: '¥9,420',
    priority: '中',
    priorityType: 'warning',
    createTime: '2026-03-17 10:25'
  },
  {
    orderNo: 'SO20260317003',
    customerName: '嘉盛供应链',
    orderType: '定制订单',
    amount: '¥26,800',
    priority: '高',
    priorityType: 'danger',
    createTime: '2026-03-17 11:40'
  },
  {
    orderNo: 'SO20260317004',
    customerName: '卓越零售',
    orderType: '渠道订单',
    amount: '¥6,580',
    priority: '低',
    priorityType: 'success',
    createTime: '2026-03-17 13:15'
  }
])

const pendingApprovals = ref([
  {
    id: 1,
    title: '采购申请审批',
    applicant: '王晓峰',
    submitTime: '今天 09:20',
    level: '紧急',
    levelType: 'danger',
    amount: '¥12,300'
  },
  {
    id: 2,
    title: '费用报销审批',
    applicant: '李梦瑶',
    submitTime: '今天 10:05',
    level: '普通',
    levelType: 'warning',
    amount: '¥2,860'
  },
  {
    id: 3,
    title: '客户折扣审批',
    applicant: '陈宇',
    submitTime: '今天 11:40',
    level: '高优',
    levelType: 'primary',
    amount: '¥8,500'
  }
])

const inventoryTodos = ref([
  {
    docNo: 'RK20260317001',
    bizType: '采购入库',
    warehouseName: '上海一号仓',
    sourceNo: 'PO20260316008',
    status: '待收货',
    statusType: 'warning',
    createTime: '2026-03-17 08:50',
    owner: '刘晨'
  },
  {
    docNo: 'CK20260317003',
    bizType: '销售出库',
    warehouseName: '杭州成品仓',
    sourceNo: 'SO20260317002',
    status: '待拣货',
    statusType: 'danger',
    createTime: '2026-03-17 10:18',
    owner: '张涛'
  },
  {
    docNo: 'DB20260317002',
    bizType: '仓库调拨',
    warehouseName: '苏州中转仓',
    sourceNo: 'TR20260317001',
    status: '待复核',
    statusType: 'primary',
    createTime: '2026-03-17 12:05',
    owner: '何静'
  },
  {
    docNo: 'CK20260317005',
    bizType: '退货出库',
    warehouseName: '上海一号仓',
    sourceNo: 'RT20260316004',
    status: '待出库',
    statusType: 'success',
    createTime: '2026-03-17 14:10',
    owner: '赵宁'
  }
])

const orderMetrics = ref({
  yesterdayOrders: 128,
  yesterdaySales: '¥186,420',
  yesterdayAvg: '¥1,456',
  yesterdayCancelled: 6,
  sevenDayOrders: 863,
  sevenDaySales: '¥1,268,900',
  sevenDayAverage: 123,
  fulfillmentRate: '96.4%'
})

const pendingOrdersPreview = computed(() => pendingOrders.value.slice(0, 2))
const pendingApprovalsPreview = computed(() => pendingApprovals.value.slice(0, 2))
const inventoryPreview = computed(() => inventoryTodos.value.slice(0, 2))

const totalPending = computed(() => {
  return pendingOrders.value.length + pendingApprovals.value.length + inventoryTodos.value.length
})

const summaryCards = computed(() => [
  {
    key: 'orders',
    title: '待处理订单',
    value: pendingOrders.value.length,
    icon: ShoppingCart,
    tagText: '订单',
    tagType: 'warning'
  },
  {
    key: 'approvals',
    title: '待处理审批',
    value: pendingApprovals.value.length,
    icon: DocumentChecked,
    tagText: '审批',
    tagType: 'danger'
  },
  {
    key: 'yesterday',
    title: '昨日订单',
    value: orderMetrics.value.yesterdayOrders,
    icon: Histogram,
    tagText: '昨日',
    tagType: 'success'
  },
  {
    key: 'inventory',
    title: '出入库待办',
    value: inventoryTodos.value.length,
    icon: Box,
    tagText: '仓储',
    tagType: 'info'
  }
])
</script>

<style scoped lang="scss">
.workbench {
  height: calc(100vh - 84px);
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(64, 158, 255, 0.18), transparent 28%),
    linear-gradient(180deg, #f4f8ff 0%, #f7f9fc 45%, #ffffff 100%);
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 22px;
  margin-bottom: 14px;
  border-radius: 18px;
  background: linear-gradient(135deg, #173b74 0%, #225bb2 52%, #3b82f6 100%);
  color: #fff;
  box-shadow: 0 18px 45px rgba(34, 91, 178, 0.22);
}

.hero-panel__eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 2px;
  opacity: 0.8;
}

.hero-panel__title {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
}

.hero-panel__desc {
  margin: 10px 0 0;
  max-width: 520px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.82);
}

.hero-panel__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 10px;
  min-width: 280px;
}

.hero-panel__meta-item {
  padding: 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1);
}

.hero-panel__meta-item .label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.hero-panel__meta-item strong {
  font-size: 24px;
}

.summary-row {
  margin-bottom: 14px;
}

.summary-card {
  border: 0;
  border-radius: 16px;
}

.summary-card :deep(.el-card__body) {
  padding: 16px 18px;
}

.summary-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.summary-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  font-size: 20px;
  color: #fff;
}

.summary-card__value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  color: #1f2a44;
}

.summary-card__title {
  margin-top: 8px;
  font-size: 14px;
  color: #344054;
}

.summary-card--orders .summary-card__icon {
  background: linear-gradient(135deg, #f59e0b, #f97316);
}

.summary-card--approvals .summary-card__icon {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.summary-card--yesterday .summary-card__icon {
  background: linear-gradient(135deg, #10b981, #059669);
}

.summary-card--inventory .summary-card__icon {
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  height: calc(100% - 190px);
}

.panel-card {
  border: 0;
  border-radius: 18px;
}

.panel-card :deep(.el-card__header) {
  padding: 16px 18px;
  border-bottom: 1px solid #eef2f6;
}

.panel-card :deep(.el-card__body) {
  padding: 16px 18px;
}

.panel-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2a44;
}

.compact-card {
  min-height: 0;
}

.metrics-card {
  min-height: 0;
}

.mini-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mini-list__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 14px;
  border: 1px solid #edf2f7;
  border-radius: 14px;
  background: #f8fbff;
}

.mini-list__title {
  font-size: 14px;
  font-weight: 600;
  color: #24324a;
}

.mini-list__meta {
  margin-top: 6px;
  font-size: 12px;
  color: #667085;
}

.mini-list__side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.mini-list__side strong {
  font-size: 14px;
  color: #1d4ed8;
}

.metrics-overview {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.metric-chip,
.metric-grid__item {
  padding: 14px 16px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f9fbff 0%, #f3f7ff 100%);
  border: 1px solid #ebf1fb;
}

.metric-chip span,
.metric-grid__item span {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: #667085;
}

.metric-chip strong,
.metric-grid__item strong {
  font-size: 22px;
  color: #1f2a44;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

@media (max-width: 1400px) {
  .content-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    height: auto;
  }

  .workbench {
    height: auto;
    overflow: auto;
  }
}

@media (max-width: 768px) {
  .hero-panel {
    flex-direction: column;
  }

  .hero-panel__meta,
  .metrics-overview,
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .mini-list__item {
    align-items: flex-start;
    flex-direction: column;
  }

  .mini-list__side {
    align-items: flex-start;
  }
}
</style>
