<template>
  <div class="dimension-selector">
    <el-card class="dimension-card" :class="{active: modelValue === 'product'}" @click="emit('update:modelValue', 'product')">
      <el-icon :size="32"><Box /></el-icon>
      <h3>按产品查看</h3>
      <p>查看每个产品在各仓库的流水</p>
    </el-card>

    <el-card class="dimension-card" :class="{active: modelValue === 'warehouse'}" @click="emit('update:modelValue', 'warehouse')">
      <el-icon :size="32"><OfficeBuilding /></el-icon>
      <h3>按仓库查看</h3>
      <p>查看每个仓库中各产品的流水</p>
    </el-card>
  </div>
</template>

<script setup>
import { Box, OfficeBuilding } from '@element-plus/icons-vue'

defineProps({
  modelValue: { type: String, default: 'product' }
})

const emit = defineEmits(['update:modelValue'])
</script>

<style lang="scss" scoped>
.dimension-selector {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.dimension-card {
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;
  padding: 24px;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0,0,0,0.1);
  }

  &.active {
    border-color: #409eff;
    background: #ecf5ff;
  }

  .el-icon {
    color: #409eff;
    margin-bottom: 12px;
  }

  h3 {
    margin: 0 0 8px;
    font-size: 16px;
    font-weight: 600;
  }

  p {
    margin: 0;
    font-size: 13px;
    color: #666;
  }
}

@media (max-width: 768px) {
  .dimension-selector {
    grid-template-columns: 1fr;
  }
}
</style>
