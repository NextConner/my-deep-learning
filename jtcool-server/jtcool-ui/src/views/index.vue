<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <el-card class="hero-card" shadow="never">
          <div class="hero-copy">
            <h2>JtCool 管理后台</h2>
            <p>
              当前项目已移除原始模板的外部宣传、社群、赞赏和演示信息，这里仅保留与项目本身相关的说明。
            </p>
            <p>
              你可以在此基础上继续补充自己的业务概览、版本记录、部署说明和运维入口。
            </p>
            <div class="hero-meta">
              <el-tag type="info">版本 v{{ version }}</el-tag>
              <el-tag>Spring Boot 3</el-tag>
              <el-tag>Vue 2</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card class="panel-card" shadow="never">
          <div slot="header">
            <span>项目概览</span>
          </div>
          <ul class="simple-list">
            <li>后端模块：jtcool-admin / common / framework / system / quartz / generator</li>
            <li>前端目录：jtcool-ui</li>
            <li>包命名空间：com.jtcool</li>
            <li>默认首页内容已切换为项目占位说明</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-gap">
      <el-col :xs="24" :md="12">
        <el-card class="panel-card" shadow="never">
          <div slot="header">
            <span>开发建议</span>
          </div>
          <ul class="simple-list">
            <li>按模块拆分业务代码，避免把业务逻辑堆在 controller 层。</li>
            <li>修改配置前先确认环境隔离方式，避免本地配置进入版本库。</li>
            <li>生成代码仅作为起点，落地前应做字段、权限和接口约束收口。</li>
          </ul>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card class="panel-card" shadow="never">
          <div slot="header">
            <span>待补充项</span>
          </div>
          <ul class="simple-list">
            <li>项目官网或内部文档地址</li>
            <li>实际部署环境信息</li>
            <li>业务联系人与维护责任人</li>
            <li>发布记录与变更说明</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "Index",
  data() {
    return {
      version: "3.9.1"
    }
  }
}
</script>

<style scoped lang="scss">
.home {
  color: #676a6c;

  .section-gap {
    margin-top: 20px;
  }

  .hero-card,
  .panel-card {
    border: 1px solid #ebeef5;
  }

  .hero-copy {
    h2 {
      margin: 0 0 16px;
      font-size: 28px;
      font-weight: 600;
      color: #303133;
    }

    p {
      line-height: 1.8;
      margin: 0 0 12px;
    }
  }

  .hero-meta {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    margin-top: 16px;
  }

  .simple-list {
    margin: 0;
    padding-left: 18px;
    line-height: 1.9;
  }
}
</style>
