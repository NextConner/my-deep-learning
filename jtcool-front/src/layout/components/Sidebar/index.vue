<template>
  <div :class="{ 'has-logo': showLogo }" class="sidebar-container">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="getMenuBackground"
        :text-color="getMenuTextColor"
        :unique-opened="true"
        :active-text-color="theme"
        :collapse-transition="false"
        mode="vertical"
        :class="sideTheme"
      >
        <sidebar-item
          v-for="(route, index) in sidebarRouters"
          :key="route.path + index"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/assets/styles/variables.module.scss'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

const route = useRoute()
const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()

const sidebarRouters = computed(() => permissionStore.sidebarRouters)
const showLogo = computed(() => settingsStore.sidebarLogo)
const sideTheme = computed(() => settingsStore.sideTheme)
const theme = computed(() => settingsStore.theme)
const isCollapse = computed(() => !appStore.sidebar.opened)

// 获取菜单背景色
const getMenuBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-bg)'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

// 获取菜单文字颜色
const getMenuTextColor = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-text)'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuText : variables.menuLightText
})

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  background-color: v-bind(getMenuBackground);
  
  .scrollbar-wrapper {
    background-color: v-bind(getMenuBackground);
  }

  .el-menu {
    border: none;
    height: 100%;
    width: 100% !important;
    padding: 10px 0; /* 增加顶部和底部的内边距 */
    
    .el-menu-item, .el-sub-menu__title {
      margin: 4px 12px; /* 增加左右间距，形成悬浮药丸效果 */
      border-radius: 8px; /* 增加柔和圆角 */
      width: calc(100% - 24px) !important; /* 修正宽度 */
      height: 48px;
      line-height: 48px;
      transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
      
      &:hover {
        background-color: var(--menu-hover, rgba(0, 0, 0, 0.04)) !important;
      }
    }

    .el-menu-item {
      color: v-bind(getMenuTextColor);
      
      &.is-active {
        color: var(--menu-active-text, #409eff);
        background-color: rgba(139, 92, 246, 0.08) !important; /* 极简的护眼高亮色 */
        font-weight: 500;
      }
    }

    .el-sub-menu__title {
      color: v-bind(getMenuTextColor);
    }
  }
}
</style>
